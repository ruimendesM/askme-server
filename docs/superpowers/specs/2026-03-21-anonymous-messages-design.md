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
- Rate limiting: IP-based + email format validation on the submit endpoint
- Admin-only access to the inbox enforced via JWT username claim (`hasAuthority("admin")`)

## Architecture

The feature lives entirely within the existing `chat` module, following its layered architecture: domain → infra (entity/repository/mapper) → service → api (dto/mapper/controller/websocket).

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

### JPA Entity
`infra/database/entities/AnonymousMessageEntity.kt`

- Table: `chat_service.anonymous_message`
- No foreign keys — no link to the users table
- Index on `createdAt` for pagination performance

### Repository
`infra/repositories/AnonymousMessageRepository.kt`

Extends `JpaRepository<AnonymousMessageEntity, AnonymousMessageId>`.
Custom query: fetch all with cursor-based pagination (before `createdAt`), ordered descending.

### Infrastructure Mapper
`infra/mappers/AnonymousMessageMappers.kt`

- `AnonymousMessageEntity → AnonymousMessage`
- `AnonymousMessage → AnonymousMessageEntity`

---

## Section 2: Service Layer

`service/AnonymousMessageService.kt`

| Method | Description |
|--------|-------------|
| `sendMessage(senderEmail: String, content: String): AnonymousMessage` | Validates email format, persists entity, publishes `AnonymousMessageReceivedEvent` |
| `getMessages(before: Instant?, pageSize: Int): List<AnonymousMessage>` | Paginated fetch for admin inbox, ordered by `createdAt` desc |

### Domain Event
`domain/event/AnonymousMessageReceivedEvent.kt`

```
messageId: AnonymousMessageId
```

Published via Spring `ApplicationEventPublisher` after the transaction commits. Consumed by `ChatWebSocketHandler` to push real-time notifications to the admin.

---

## Section 3: API Layer

### Controller
`api/controllers/AnonymousMessageController.kt`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/anonymous-messages` | Public | Submit anonymous message |
| `GET` | `/api/anonymous-messages` | `hasAuthority("admin")` | Fetch admin inbox (paginated) |

**POST** returns `201 Created` with no body.
**GET** accepts optional query params `before: Instant?` and `pageSize: Int?`.

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
senderEmail: String  // @Email validation
content: String      // @NotBlank, reasonable max length
```

### API Mapper
`api/mappers/AnonymousMessageDtoMappers.kt`
- `AnonymousMessage → AnonymousMessageDto`

### Security Config Update
`app/.../SecurityConfig.kt`

- Add `POST /api/anonymous-messages` to `permitAll()`
- Add `GET /api/anonymous-messages` with `.hasAuthority("admin")`

### Rate Limiting
IP-based rate limiting applied to `POST /api/anonymous-messages` at the controller level, consistent with existing rate limiting patterns in the app.

---

## Section 4: WebSocket

### New Outgoing Message Type
Add `NEW_ANONYMOUS_MESSAGE` to `OutgoingWebSocketMessageType` in `api/dto/ws/WebSocketEvent.kt`.

### Handler Extension
`ChatWebSocketHandler` gains one new `@TransactionalEventListener`:

```
onAnonymousMessage(AnonymousMessageReceivedEvent)
```

- Loads `AnonymousMessage` from `AnonymousMessageService`
- Maps to `AnonymousMessageDto`
- Broadcasts `NEW_ANONYMOUS_MESSAGE` frame **only** to the admin's WebSocket sessions (looked up by admin's `UserId` in the existing `userToSessions` map)

No new WebSocket endpoint or handler needed.

---

## Section 5: JWT Evolution

Three small changes to add username as a JWT claim, enabling Spring Security authority-based admin checks.

### `JwtService` (common module)
- `generateAccessToken(userId: UserId, username: String)` — adds `"username"` as a JWT claim

### `AuthService` (user module)
- Update `generateAccessToken` call site to pass the user's `username`

### `JwtAuthFilter` (user module)
- After extracting `userId`, read the `"username"` claim
- Construct `UsernamePasswordAuthenticationToken` with `listOf(SimpleGrantedAuthority(username))` instead of `emptyList()`

This enables `hasAuthority("admin")` in Spring Security config without DB lookups in the auth filter.

---

## Out of Scope

- Admin replies to anonymous users
- Read/unread tracking
- Admin deleting anonymous messages
- Email-based dedup within time windows (IP rate limiting is sufficient for now)
