package com.ruimendes.askme.domain.events.user

import com.ruimendes.askme.domain.events.AskmeEvent
import com.ruimendes.askme.domain.type.UserId
import java.time.Instant
import java.util.*

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): AskmeEvent {

    data class Created(
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_CREATED_KEY
    ): UserEvent()

    data class Verified(
        val userId: UserId,
        val email: String,
        val username: String,
        override val eventKey: String = UserEventConstants.USER_VERIFIED_KEY
    ): UserEvent()

    data class RequestResendVerification(
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESEND_VERIFICATION_KEY
    ): UserEvent()

    data class RequestResetPassword(
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        val expiresInMinutes: Long,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESET_PASSWORD_KEY
    ): UserEvent()
}