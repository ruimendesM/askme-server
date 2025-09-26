package com.ruimendes.askme.service.auth

import com.ruimendes.askme.domain.exception.InvalidTokenException
import com.ruimendes.askme.domain.exception.UserNotFoundException
import com.ruimendes.askme.domain.model.EmailVerificationToken
import com.ruimendes.askme.infra.database.entities.EmailVerificationTokenEntity
import com.ruimendes.askme.infra.database.mappers.toEmailVerificationToken
import com.ruimendes.askme.infra.database.repository.EmailVerificationTokenRepository
import com.ruimendes.askme.infra.database.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${askme.email.verification.expiry-hours}") private val expiryHours: Long
) {

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

       emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token has already been used")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token has expired")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )
        userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}