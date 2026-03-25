package com.ruimendes.askme.infra.mappers

import com.ruimendes.askme.infra.database.entities.AnonymousMessageEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AnonymousMessageMappersTest {

    @Test
    fun `toAnonymousMessage maps all fields correctly`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val entity = AnonymousMessageEntity(
            id = id,
            senderEmail = "user@example.com",
            content = "Hello admin",
            createdAt = now
        )

        val domain = entity.toAnonymousMessage()

        assertEquals(id, domain.id)
        assertEquals("user@example.com", domain.senderEmail)
        assertEquals("Hello admin", domain.content)
        assertEquals(now, domain.createdAt)
    }
}
