package com.ruimendes.askme.service

import com.ruimendes.askme.api.dto.ChatMessageDto
import com.ruimendes.askme.api.mappers.toChatMessageDto
import com.ruimendes.askme.domain.event.MessageDeletedEvent
import com.ruimendes.askme.domain.events.chat.ChatEvent
import com.ruimendes.askme.domain.exception.ChatMessageNotFoundException
import com.ruimendes.askme.domain.exception.ChatNotFoundException
import com.ruimendes.askme.domain.exception.ChatParticipantNotFoundException
import com.ruimendes.askme.domain.exception.ForbiddenException
import com.ruimendes.askme.domain.models.ChatMessage
import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.ChatMessageId
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.database.entities.ChatMessageEntity
import com.ruimendes.askme.infra.mappers.toChatMessage
import com.ruimendes.askme.infra.message_queue.EventPublisher
import com.ruimendes.askme.infra.repositories.ChatMessageRepository
import com.ruimendes.askme.infra.repositories.ChatParticipantRepository
import com.ruimendes.askme.infra.repositories.ChatRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw ChatMessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )

        evictMessagesCache(message.chatId)
    }

    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let Spring handle the cache evict
    }

}