package com.ruimendes.askme.infra.message_queue

import com.ruimendes.askme.domain.events.chat.ChatEvent
import com.ruimendes.askme.domain.events.user.UserEvent
import com.ruimendes.askme.infra.service.EmailService
import com.ruimendes.askme.infra.service.PushNotificationService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NotificationChatEventListener(private val pushNotificationService: PushNotificationService) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_CHAT_EVENTS])
    fun handleUserEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.NewMessage -> {
                pushNotificationService.sendNewMessageNotifications(
                    recipientUserIds = event.recipientIds.toList(),
                    senderUserId = event.senderId,
                    senderUsername = event.senderUsername,
                    message = event.message,
                    chatId = event.chatId
                )
            }
        }
    }
}