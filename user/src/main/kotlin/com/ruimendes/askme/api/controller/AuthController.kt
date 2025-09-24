package com.ruimendes.askme.api.controller

import com.ruimendes.askme.api.dto.AuthenticatedUserDto
import com.ruimendes.askme.api.dto.LoginRequest
import com.ruimendes.askme.api.dto.RegisterRequest
import com.ruimendes.askme.api.dto.UserDto
import com.ruimendes.askme.api.mappers.toAuthenticatedUserDto
import com.ruimendes.askme.api.mappers.toUserDto
import com.ruimendes.askme.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

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

    @PostMapping("login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }
}