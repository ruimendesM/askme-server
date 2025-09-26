package com.ruimendes.askme.api.controller

import com.ruimendes.askme.api.dto.*
import com.ruimendes.askme.api.mappers.toAuthenticatedUserDto
import com.ruimendes.askme.api.mappers.toUserDto
import com.ruimendes.askme.service.auth.AuthService
import com.ruimendes.askme.service.auth.EmailVerificationService
import com.ruimendes.askme.service.auth.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService
            .register(
                email = body.email,
                username = body.username,
                password = body.password
            )
            .toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService
            .refresh(body.refreshToken)
            .toAuthenticatedUserDto()
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String,
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            passwordResetToken = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        // TODO: extract user id and call service
    }
}