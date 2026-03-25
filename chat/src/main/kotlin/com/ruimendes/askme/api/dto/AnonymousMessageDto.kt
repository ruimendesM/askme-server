package com.ruimendes.askme.api.dto

import com.ruimendes.askme.domain.type.AnonymousMessageId
import java.time.Instant

data class AnonymousMessageDto(
    val id: AnonymousMessageId,
    val senderEmail: String,
    val content: String,
    val createdAt: Instant
)
