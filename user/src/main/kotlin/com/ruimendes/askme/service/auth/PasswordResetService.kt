package com.ruimendes.askme.service.auth

import com.ruimendes.askme.domain.exception.InvalidCredentialsException
import com.ruimendes.askme.domain.exception.InvalidTokenException
import com.ruimendes.askme.domain.exception.SamePasswordException
import com.ruimendes.askme.domain.exception.UserNotFoundException
import com.ruimendes.askme.domain.model.UserId
import com.ruimendes.askme.infra.database.entities.PasswordResetTokenEntity
import com.ruimendes.askme.infra.database.repository.PasswordResetTokenRepository
import com.ruimendes.askme.infra.database.repository.RefreshTokenRepository
import com.ruimendes.askme.infra.database.repository.UserRepository
import com.ruimendes.askme.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${askme.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service about password reset trigger to send email
    }

    @Transactional
    fun resetPassword(passwordResetToken: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(passwordResetToken)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Email verification token has already been used")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Email verification token has expired")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashedNewPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )
        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}