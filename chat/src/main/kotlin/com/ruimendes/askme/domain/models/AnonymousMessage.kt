package com.ruimendes.askme.domain.models

import com.ruimendes.askme.domain.type.AnonymousMessageId
import java.time.Instant

data class AnonymousMessage(
    val id: AnonymousMessageId,
    val senderEmail: String,
    val content: String,
    val createdAt: Instant
)
