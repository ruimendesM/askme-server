package com.ruimendes.askme.service

import com.ruimendes.askme.api.dto.ChatMessageDto
import com.ruimendes.askme.api.mappers.toChatMessageDto
import com.ruimendes.askme.domain.event.ChatCreatedEvent
import com.ruimendes.askme.domain.event.ChatParticipantLeftEvent
import com.ruimendes.askme.domain.event.ChatParticipantsJoinedEvent
import com.ruimendes.askme.domain.exception.ChatNotFoundException
import com.ruimendes.askme.domain.exception.ChatParticipantNotFoundException
import com.ruimendes.askme.domain.exception.ForbiddenException
import com.ruimendes.askme.domain.exception.InvalidChatSizeException
import com.ruimendes.askme.domain.models.Chat
import com.ruimendes.askme.domain.models.ChatMessage
import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.database.entities.ChatEntity
import com.ruimendes.askme.infra.mappers.toChat
import com.ruimendes.askme.infra.mappers.toChatMessage
import com.ruimendes.askme.infra.repositories.ChatMessageRepository
import com.ruimendes.askme.infra.repositories.ChatParticipantRepository
import com.ruimendes.askme.infra.repositories.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant? = null,
        pageSize: Int
    ): List<ChatMessageDto> { // Using ChatMessageDto for cache purposes
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    fun getChatById(chatId: ChatId, requestUserId: UserId): Chat? {
        return chatRepository.findChatById(chatId, requestUserId)
            ?.toChat(lastMessageForChat(chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map {
                it.toChat(lastMessage = latestMessages[it.id]?.toChatMessage())
            }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {
        if (otherUserIds.isEmpty()) {
            throw InvalidChatSizeException()
        }
        val otherParticipants = chatParticipantRepository.findByUserIdIn(otherUserIds)

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = mutableSetOf(creator).apply { addAll(otherParticipants) }
            )
        ).toChat().also { entity ->
            applicationEventPublisher.publishEvent(
                ChatCreatedEvent(
                    chatId = entity.id,
                    participantsIds = entity.participants.map { it.userId }
                )
            )
        }
    }

    @Transactional
    fun addParticipantToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }
        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants.plus(users).toMutableSet()

            }
        ).toChat(lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = userIds
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId,
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(chat.apply {
            this.participants = chat.participants.minus(participant).toMutableSet()
        })

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()

    }
}