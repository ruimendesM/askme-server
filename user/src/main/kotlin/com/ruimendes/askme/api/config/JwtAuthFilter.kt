package com.ruimendes.askme.api.config

import com.ruimendes.askme.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            if (jwtService.validateAccessToken(authHeader)) {
                val userId = jwtService.getUserIdFromToken(authHeader)
                val role = jwtService.getRoleFromToken(authHeader)
                val authorities = if (role != null) listOf(SimpleGrantedAuthority(role)) else emptyList()
                val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }
}