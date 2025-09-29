package com.ruimendes.askme.infra.messaging

import com.ruimendes.askme.domain.events.user.UserEvent
import com.ruimendes.askme.domain.models.ChatParticipant
import com.ruimendes.askme.infra.message_queue.MessageQueues
import com.ruimendes.askme.service.ChatParticipantService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(private val chatParticipantService: ChatParticipantService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when(event) {
            is UserEvent.Verified -> {
                logger.info("Received user event: {}", event)
                chatParticipantService.createChatParticipant(
                    ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null
                    )
                )
            }
            else -> Unit // Ignore other events
        }
    }
}