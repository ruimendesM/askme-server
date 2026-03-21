package com.ruimendes.askme.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Base64
import java.util.UUID

class JwtServiceTest {

    // 256-bit key for testing — must be Base64-encoded
    private val testSecretBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() })
    private val jwtService = JwtService(secretBase64 = testSecretBase64, expirationMinutes = 30)

    @Test
    fun `generateAccessToken includes role claim`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, role = "ADMIN")

        val role = jwtService.getRoleFromToken(token)

        assertEquals("ADMIN", role)
    }

    @Test
    fun `generateAccessToken includes USER role`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, role = "USER")

        val role = jwtService.getRoleFromToken(token)

        assertEquals("USER", role)
    }

    @Test
    fun `getUserIdFromToken still works after role addition`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, role = "USER")

        val extractedId = jwtService.getUserIdFromToken(token)

        assertEquals(userId, extractedId)
    }

    @Test
    fun `getRoleFromToken returns null for refresh token (no role claim)`() {
        val userId = UUID.randomUUID()
        val refreshToken = jwtService.generateRefreshToken(userId)

        val role = jwtService.getRoleFromToken(refreshToken)

        assertNull(role)
    }
}
