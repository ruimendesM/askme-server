package com.ruimendes.askme.infra.database.entities

import com.ruimendes.askme.domain.type.AnonymousMessageId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "anonymous_message",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_anonymous_message_created_at",
            columnList = "created_at DESC"
        )
    ]
)
class AnonymousMessageEntity(
    @Id
    var id: AnonymousMessageId? = null,
    @Column(name = "sender_email", nullable = false, length = 320)
    var senderEmail: String,
    @Column(nullable = false, length = 2000)
    var content: String,
    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)
