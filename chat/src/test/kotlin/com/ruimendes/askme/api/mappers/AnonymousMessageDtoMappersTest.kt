package com.ruimendes.askme.api.mappers

import com.ruimendes.askme.domain.models.AnonymousMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AnonymousMessageDtoMappersTest {

    @Test
    fun `toAnonymousMessageDto maps all fields correctly`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val domain = AnonymousMessage(
            id = id,
            senderEmail = "hello@test.com",
            content = "some content",
            createdAt = now
        )

        val dto = domain.toAnonymousMessageDto()

        assertEquals(id, dto.id)
        assertEquals("hello@test.com", dto.senderEmail)
        assertEquals("some content", dto.content)
        assertEquals(now, dto.createdAt)
    }
}
