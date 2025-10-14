package com.ruimendes.askme.infra.service

import com.ruimendes.askme.domain.exception.InvalidDeviceTokenException
import com.ruimendes.askme.domain.model.DeviceToken
import com.ruimendes.askme.domain.model.PushNotification
import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.database.DeviceTokenEntity
import com.ruimendes.askme.infra.database.DeviceTokenRepository
import com.ruimendes.askme.infra.mappers.toDeviceToken
import com.ruimendes.askme.infra.mappers.toPlatformEntity
import com.ruimendes.askme.infra.push_notification.FirebasePushNotificationService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {

    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java.name)

    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: DeviceToken.Platform
    ): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token)

        val trimmedToken = token.trim()
        if (existing == null && !firebasePushNotificationService.isValidToken(trimmedToken)) {
            throw InvalidDeviceTokenException()
        }

        val entity = if (existing != null) {
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity(),
                )
            )
        }

        return entity.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(token: String) {
        deviceTokenRepository.deleteByToken(token.trim())
    }

    fun sendNewMessageNotifications(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        message: String,
        chatId: ChatId
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found for users: $recipientUserIds")
            return
        }

        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            title = "New message from $senderUsername", // TODO this should be localized
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )

        firebasePushNotificationService.sendNotification(notification)
    }
}