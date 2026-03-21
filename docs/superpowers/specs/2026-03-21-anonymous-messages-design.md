# Anonymous Messages Feature Design

**Date:** 2026-03-21
**Status:** Approved

## Overview

Extend the Spring Boot chat server to allow anonymous (non-registered) users to send one-way messages to the `admin` account. Anonymous users must provide an email address. The admin sees these messages in a separate inbox, with real-time delivery via WebSocket.

## Requirements

- Anonymous users (no account) can send messages to admin via a public REST endpoint
- A sender email is required; no other registration is needed
- Messages are one-way: admin cannot reply
- Admin sees anonymous messages in a separate inbox (not mixed with normal chats)
- New messages are pushed to the admin in real-time via WebSocket when they are connected
- Rate limiting: IP-based (5 requests per 10 minutes per IP) on the submit endpoint
- Email format validated via `@Email` on the request DTO
- Admin-only access to the inbox enforced via a `role` JWT claim (`hasAuthority("ADMIN")`)

## Architecture

The feature lives entirely within the existing `chat` module, following its layered architecture: domain → infra (entity/repository/mapper) → service → api (dto/mapper/controller/websocket). JWT changes touch `common` and `user` modules.

---

## Section 1: Data Model

### Domain Model
`domain/models/AnonymousMessage.kt`

```
id: AnonymousMessageId (UUID)
senderEmail: String
content: String
createdAt: Instant
```

`AnonymousMessageId` is a typealias for `UUID`, defined in `common/src/main/kotlin/com/ruimendes/askme/domain/type/` alongside existing ID types (`UserId`, `ChatId`, etc.).

### JPA Entity
`infra/database/entities/AnonymousMessageEntity.kt`

- Table: `chat_service.anonymous_message`
- No foreign keys — no link to the users table
- Column `sender_email VARCHAR(320)` (RFC 5321 max length for email addresses)
- Column `content VARCHAR(2000)`
- Index on `created_at` for pagination performance
- Schema is managed by Hibernate `ddl-auto` in development (matching the existing project convention — no Flyway/Liquibase). In production environments, the table must be created manually via a DDL script before deployment.

### Repository
`infra/repositories/AnonymousMessageRepository.kt`

Extends `JpaRepository<AnonymousMessageEntity, AnonymousMessageId>`.

Custom query method, consistent with the pattern in `ChatMessageRepository.findByChatIdBefore`:

```kotlin
fun findByCreatedAtBefore(before: Instant, pageable: Pageable): Slice<AnonymousMessageEntity>
```

`pageSize` from the service is converted to a `PageRequest.of(0, pageSize, Sort.by("createdAt").descending())` pageable. When the caller passes `before = null`, the service substitutes `Instant.now()` so this single repository method handles both the initial load and subsequent pages. `findById` is built-in JPA.

### Infrastructure Mapper
`infra/mappers/AnonymousMessageMappers.kt`

- `AnonymousMessageEntity → AnonymousMessage`
- `AnonymousMessage → AnonymousMessageEntity`

---

## Section 2: Service Layer

`service/AnonymousMessageService.kt`

All write methods are `@Transactional`. `sendMessage` must be transactional to ensure the `@TransactionalEventListener(AFTER_COMMIT)` in the WebSocket handler fires only after the row is committed.

| Method | Description |
|--------|-------------|
| `sendMessage(senderEmail: String, content: String): AnonymousMessage` | Persists entity, publishes `AnonymousMessageReceivedEvent` via `applicationEventPublisher`. Email and content validation is handled by Bean Validation at the controller layer before this method is called. |
| `getMessages(before: Instant?, pageSize: Int = 20): List<AnonymousMessage>` | Paginated fetch. Default page size 20, max 100. Uses `findByCreatedAtBefore` with `Pageable` when `before` is provided, or fetches the latest page when `before` is null. Returns `List` (extracted from `Slice`). |
| `getById(id: AnonymousMessageId): AnonymousMessage` | Fetch single message by ID — used by the WebSocket handler after the event fires. If the ID is not found, logs a warning and returns null; the caller silently skips broadcast. Non-throwing behaviour is required here because this runs inside a `@TransactionalEventListener` where unhandled exceptions would be swallowed by the event infrastructure anyway. |

### Domain Event
`domain/event/AnonymousMessageReceivedEvent.kt`

```
messageId: AnonymousMessageId
```

This is an **in-process Spring `ApplicationEventPublisher` event** — the same kind as `ChatCreatedEvent`, `MessageDeletedEvent`, etc. It is NOT published via RabbitMQ / the `EventPublisher` used for cross-module events. The event carries only the ID to avoid transporting detached JPA objects across the transaction boundary (consistent with existing events).

---

## Section 3: API Layer

### Controller
`api/controllers/AnonymousMessageController.kt`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/anonymous-messages` | Public | Submit anonymous message |
| `GET` | `/api/anonymous-messages` | `hasAuthority("ADMIN")` | Fetch admin inbox (paginated) |

**POST** — accepts `@Valid @RequestBody CreateAnonymousMessageRequest`. Returns `201 Created` with no body on success.

- Bean Validation failure (`@Email`, `@NotBlank`, `@Size`) produces `400 Bad Request` via Spring Boot's default `MethodArgumentNotValidException` handling. A new `@ExceptionHandler(MethodArgumentNotValidException::class)` must be added to `CommonExceptionHandler` to produce a response consistent with the project's `{ "code": ..., "message": ... }` error format.
- Rate limit exceeded returns `429 Too Many Requests` (existing `@IpRateLimit` behaviour).

Apply `@IpRateLimit(requests = 5, duration = 10, unit = TimeUnit.MINUTES)` to the `POST` handler method.

**GET** — query params: `before: Instant?` (ISO-8601 timestamp, optional) and `pageSize: Int?` (optional, default 20, capped at 100). Returns `200 OK` with `List<AnonymousMessageDto>`. No next-page cursor in the response — cursor is the `createdAt` of the last item in the returned list, matching the existing chat message pagination pattern. When `before` is absent, the service passes `Instant.now()` as the sentinel value to retrieve the most recent page.

### DTOs
`api/dto/AnonymousMessageDto.kt`
```
id: UUID
senderEmail: String
content: String
createdAt: Instant
```

`api/dto/CreateAnonymousMessageRequest.kt`
```
senderEmail: String   // @Email, @Size(max = 320)
content: String       // @NotBlank, @Size(max = 2000)
```

`senderEmail` is trimmed and lowercased before persistence to normalise equivalent addresses.

### API Mapper
`api/mappers/AnonymousMessageDtoMappers.kt`
- `AnonymousMessage → AnonymousMessageDto`

### Security Config Update
`app/.../SecurityConfig.kt`

New matchers must be inserted **before** the existing `anyRequest().authenticated()` catch-all (Spring Security evaluates rules in declaration order):

```kotlin
.requestMatchers(HttpMethod.POST, "/api/anonymous-messages").permitAll()
.requestMatchers(HttpMethod.GET, "/api/anonymous-messages").hasAuthority("ADMIN")
```

---

## Section 4: WebSocket

### New Outgoing Message Type
Add `NEW_ANONYMOUS_MESSAGE` to `OutgoingWebSocketMessageType` in `api/dto/ws/WebSocketEvent.kt`.

### Handler Extension
`ChatWebSocketHandler` gains one new `@TransactionalEventListener(phase = AFTER_COMMIT)`:

```
onAnonymousMessage(AnonymousMessageReceivedEvent)
```

Steps:
1. Call `anonymousMessageService.getById(event.messageId)` — if null, log and return
2. Map to `AnonymousMessageDto` via the API mapper
3. Construct `OutgoingWebSocketMessage(type = NEW_ANONYMOUS_MESSAGE, payload = jsonMapper.writeValueAsString(dto))`
4. Serialise the wrapper: `jsonMapper.writeValueAsString(outgoingMessage)`
5. Send the resulting string to the admin's sessions via `userToSessions[adminUserId]`

This two-step serialisation (DTO → JSON string as payload, then the whole message → JSON string) matches the existing pattern used by all other event handlers in `ChatWebSocketHandler`.

**Admin session lookup:** The admin's `UserId` is injected from `@ConfigurationProperties` class `AdminProperties(userId: UserId)` bound to prefix `admin` in `application.yml`. This class is annotated with `@Validated` and `@NotNull` on `userId` so a missing or invalid value fails fast at startup with a clear error. `UserId` is a plain `UUID` typealias; Spring Boot's `@ConfigurationProperties` binds `UUID` from a string by default — no custom converter is needed. `AdminProperties.userId` is the same `UserId` type used as the key in `userToSessions: HashMap<UserId, MutableSet<String>>`, so the lookup is a direct key match with no type conversion. If the admin is not connected, the event is silently dropped (consistent with existing offline-user behaviour).

No new WebSocket endpoint or handler needed.

---

## Section 5: JWT Evolution

### `JwtService` (common module)
- `generateAccessToken(userId: UserId, role: String)` — adds `"role"` as a JWT claim (value: `"ADMIN"` or `"USER"`)

### `AuthService` (user module)
The single shared helper `UserEntity.generateTokensAndCreateAuthenticatedUser()` (line 113 in `AuthService.kt`) is the **only call site** that needs updating — both `login` and `refresh` call this helper and no other token issuance paths exist. Update this helper to derive the role (`if (username == "admin") "ADMIN" else "USER"`) and pass it to `generateAccessToken`. Any live refresh token issued before this change will still produce a correct role-bearing access token on the next refresh, since the refresh path also goes through this helper.

### `JwtAuthFilter` (user module)
- After extracting `userId`, read the `"role"` claim
- If the claim is present: `UsernamePasswordAuthenticationToken(userId, null, listOf(SimpleGrantedAuthority(role)))`
- If the claim is absent (existing tokens issued before this change): `UsernamePasswordAuthenticationToken(userId, null, emptyList())` — graceful degradation, no regression
- Existing tokens remain valid but carry no authority until they expire or the user calls refresh. No forced logout.

---

## Section 6: Admin Identity

The admin account is a pre-seeded user with unique username `"admin"`. Its `UserId` (UUID) is configured in `application.yml` under the `admin.user-id` property. This is bound via `AdminProperties` (see Section 4) and validated at startup. If the admin account is recreated in a new environment, this config value must be updated.

---

## Out of Scope

- Admin replies to anonymous users
- Read/unread tracking
- Admin deleting anonymous messages
- Email-based dedup within time windows (IP rate limiting is sufficient for now)
