package com.ruimendes.askme.service

import com.ruimendes.askme.domain.event.AnonymousMessageReceivedEvent
import com.ruimendes.askme.domain.models.AnonymousMessage
import com.ruimendes.askme.infra.database.entities.AnonymousMessageEntity
import com.ruimendes.askme.infra.repositories.AnonymousMessageRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AnonymousMessageServiceTest {

    @Mock lateinit var repository: AnonymousMessageRepository
    @Mock lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks lateinit var service: AnonymousMessageService

    @Test
    fun `sendMessage persists normalised email and content`() {
        val entity = AnonymousMessageEntity(
            id = UUID.randomUUID(),
            senderEmail = "user@example.com",
            content = "hello",
            createdAt = Instant.now()
        )
        whenever(repository.save(any<AnonymousMessageEntity>())).thenReturn(entity)

        service.sendMessage(senderEmail = "  User@EXAMPLE.COM  ", content = "  hello  ")

        val captor = argumentCaptor<AnonymousMessageEntity>()
        verify(repository).save(captor.capture())
        assertEquals("user@example.com", captor.firstValue.senderEmail)
        assertEquals("hello", captor.firstValue.content)
    }

    @Test
    fun `sendMessage publishes AnonymousMessageReceivedEvent after save`() {
        val id = UUID.randomUUID()
        val entity = AnonymousMessageEntity(
            id = id,
            senderEmail = "a@b.com",
            content = "hi",
            createdAt = Instant.now()
        )
        whenever(repository.save(any<AnonymousMessageEntity>())).thenReturn(entity)

        service.sendMessage("a@b.com", "hi")

        val eventCaptor = argumentCaptor<AnonymousMessageReceivedEvent>()
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals(id, eventCaptor.firstValue.messageId)
    }

    @Test
    fun `getMessages uses Instant-now sentinel when before is null and caps pageSize at 100`() {
        whenever(repository.findByCreatedAtBefore(any(), any())).thenReturn(SliceImpl(emptyList()))

        service.getMessages(before = null, pageSize = 200)

        val pageableCaptor = argumentCaptor<org.springframework.data.domain.Pageable>()
        val instantCaptor = argumentCaptor<Instant>()
        verify(repository).findByCreatedAtBefore(instantCaptor.capture(), pageableCaptor.capture())
        assertEquals(100, pageableCaptor.firstValue.pageSize)
        // before should be close to now (within 5 seconds)
        assertTrue(instantCaptor.firstValue.isAfter(Instant.now().minusSeconds(5)))
    }

    @Test
    fun `getById returns mapped domain object when found`() {
        val id = UUID.randomUUID()
        val entity = AnonymousMessageEntity(
            id = id,
            senderEmail = "x@y.com",
            content = "msg",
            createdAt = Instant.now()
        )
        whenever(repository.findById(id)).thenReturn(Optional.of(entity))

        val result: AnonymousMessage? = service.getById(id)

        assertNotNull(result)
        assertEquals(id, result!!.id)
    }

    @Test
    fun `getById returns null when not found`() {
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(Optional.empty())

        val result = service.getById(id)

        assertNull(result)
    }
}
