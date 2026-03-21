package com.ruimendes.askme.service

import com.ruimendes.askme.domain.event.AnonymousMessageReceivedEvent
import com.ruimendes.askme.domain.models.AnonymousMessage
import com.ruimendes.askme.domain.type.AnonymousMessageId
import com.ruimendes.askme.infra.database.entities.AnonymousMessageEntity
import com.ruimendes.askme.infra.mappers.toAnonymousMessage
import com.ruimendes.askme.infra.repositories.AnonymousMessageRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AnonymousMessageService(
    private val anonymousMessageRepository: AnonymousMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun sendMessage(senderEmail: String, content: String): AnonymousMessage {
        val entity = anonymousMessageRepository.save(
            AnonymousMessageEntity(
                id = UUID.randomUUID(),
                senderEmail = senderEmail.trim().lowercase(),
                content = content.trim()
            )
        )

        applicationEventPublisher.publishEvent(
            AnonymousMessageReceivedEvent(messageId = entity.id!!)
        )

        return entity.toAnonymousMessage()
    }

    fun getMessages(before: Instant? = null, pageSize: Int = 20): List<AnonymousMessage> {
        val effectiveBefore = before ?: Instant.now()
        val effectivePageSize = pageSize.coerceAtMost(100)
        // Sort is handled by the JPQL ORDER BY clause — do NOT add Sort to PageRequest here
        // to avoid a double ORDER BY, consistent with the existing ChatMessageRepository pattern.
        val pageable = PageRequest.of(0, effectivePageSize)
        return anonymousMessageRepository.findByCreatedAtBefore(effectiveBefore, pageable)
            .content
            .map { it.toAnonymousMessage() }
    }

    fun getById(id: AnonymousMessageId): AnonymousMessage? {
        return anonymousMessageRepository.findByIdOrNull(id)
            ?.toAnonymousMessage()
            ?: run {
                logger.warn("AnonymousMessage with id $id not found — skipping WebSocket broadcast")
                null
            }
    }
}
