package com.ruimendes.askme.service.auth

import com.ruimendes.askme.domain.exception.*
import com.ruimendes.askme.domain.model.AuthenticatedUser
import com.ruimendes.askme.domain.model.User
import com.ruimendes.askme.domain.model.UserId
import com.ruimendes.askme.infra.database.entities.RefreshTokenEntity
import com.ruimendes.askme.infra.database.entities.UserEntity
import com.ruimendes.askme.infra.database.mappers.toUser
import com.ruimendes.askme.infra.database.repository.RefreshTokenRepository
import com.ruimendes.askme.infra.database.repository.UserRepository
import com.ruimendes.askme.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService
) {

    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()
        val user = userRepository.findByEmailOrUsername(trimmedEmail, username.trim())
        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password)
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(trimmedEmail)

        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if(!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.generateTokensAndCreateAuthenticatedUser()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let {
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = it,
                hashedToken = hashed
            ) ?: throw InvalidTokenException("Invalid refresh token")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = it,
                hashedToken = hashed
            )

            user.generateTokensAndCreateAuthenticatedUser()
        } ?: throw UserNotFoundException()
    }

    private fun UserEntity.generateTokensAndCreateAuthenticatedUser(): AuthenticatedUser {
        val userId = id ?: throw UserNotFoundException()

        val accessToken = jwtService.generateAccessToken(userId)
        val refreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(userId, refreshToken)

        return AuthenticatedUser(
            user = this.toUser(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}