package com.ruimendes.askme.api.security

import com.ruimendes.askme.api.config.JwtAuthFilter
import com.ruimendes.askme.infra.rate_limiting.EmailRateLimiter
import com.ruimendes.askme.infra.rate_limiting.IpRateLimiter
import com.ruimendes.askme.infra.rate_limiting.IpResolver
import com.ruimendes.askme.infra.service.PushNotificationService
import com.ruimendes.askme.service.AnonymousMessageService
import com.ruimendes.askme.service.ChatMessageService
import com.ruimendes.askme.service.ChatParticipantService
import com.ruimendes.askme.service.ChatService
import com.ruimendes.askme.service.ProfilePictureService
import com.ruimendes.askme.service.auth.AuthService
import com.ruimendes.askme.service.auth.EmailVerificationService
import com.ruimendes.askme.service.auth.PasswordResetService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@Import(SecurityConfig::class)
@TestPropertySource(
    properties = [
        "askme.cors.allowed-origin=http://localhost:3000",
        "askme.rate-limit.ip.apply-limit=false"
    ]
)
class CorsConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @MockitoBean
    private lateinit var anonymousMessageService: AnonymousMessageService

    @MockitoBean
    private lateinit var chatService: ChatService

    @MockitoBean
    private lateinit var chatMessageService: ChatMessageService

    @MockitoBean
    private lateinit var chatParticipantService: ChatParticipantService

    @MockitoBean
    private lateinit var profilePictureService: ProfilePictureService

    @MockitoBean
    private lateinit var pushNotificationService: PushNotificationService

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var emailVerificationService: EmailVerificationService

    @MockitoBean
    private lateinit var passwordResetService: PasswordResetService

    @MockitoBean
    private lateinit var emailRateLimiter: EmailRateLimiter

    @MockitoBean
    private lateinit var ipRateLimiter: IpRateLimiter

    @MockitoBean
    private lateinit var ipResolver: IpResolver

    @BeforeEach
    fun setUp() {
        doAnswer { invocation ->
            val chain = invocation.getArgument<FilterChain>(2)
            chain.doFilter(
                invocation.getArgument<ServletRequest>(0),
                invocation.getArgument<ServletResponse>(1)
            )
        }.`when`(jwtAuthFilter).doFilter(any(), any(), any())
    }

    @Test
    fun `preflight request from allowed origin returns 200 with CORS headers`() {
        mockMvc.perform(
            options("/api/anonymous-messages")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
    }

    @Test
    fun `preflight request from disallowed origin is rejected`() {
        mockMvc.perform(
            options("/api/anonymous-messages")
                .header("Origin", "http://evil.com")
                .header("Access-Control-Request-Method", "POST")
        )
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
    }
}
