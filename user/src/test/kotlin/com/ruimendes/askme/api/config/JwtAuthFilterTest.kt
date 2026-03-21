package com.ruimendes.askme.api.config

import com.ruimendes.askme.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JwtAuthFilterTest {

    @Mock lateinit var jwtService: JwtService
    @Mock lateinit var request: HttpServletRequest
    @Mock lateinit var response: HttpServletResponse
    @Mock lateinit var filterChain: FilterChain

    @InjectMocks lateinit var filter: JwtAuthFilter

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `token with role sets SimpleGrantedAuthority in SecurityContext`() {
        val token = "Bearer valid-token"
        val userId = UUID.randomUUID()
        `when`(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token)
        `when`(jwtService.validateAccessToken(token)).thenReturn(true)
        `when`(jwtService.getUserIdFromToken(token)).thenReturn(userId)
        `when`(jwtService.getRoleFromToken(token)).thenReturn("ADMIN")

        filter.doFilter(request, response, filterChain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals(userId, auth!!.principal)
        assertTrue(auth.authorities.contains(SimpleGrantedAuthority("ADMIN")))
    }

    @Test
    fun `token without role (null) sets empty authorities`() {
        val token = "Bearer old-token"
        val userId = UUID.randomUUID()
        `when`(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token)
        `when`(jwtService.validateAccessToken(token)).thenReturn(true)
        `when`(jwtService.getUserIdFromToken(token)).thenReturn(userId)
        `when`(jwtService.getRoleFromToken(token)).thenReturn(null)

        filter.doFilter(request, response, filterChain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertTrue(auth!!.authorities.isEmpty())
    }

    @Test
    fun `missing Authorization header does not set authentication`() {
        `when`(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null)

        filter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
    }
}
