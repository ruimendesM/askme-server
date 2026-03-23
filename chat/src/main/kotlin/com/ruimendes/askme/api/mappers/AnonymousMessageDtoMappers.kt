package com.ruimendes.askme.api.mappers

import com.ruimendes.askme.api.dto.AnonymousMessageDto
import com.ruimendes.askme.domain.models.AnonymousMessage

fun AnonymousMessage.toAnonymousMessageDto(): AnonymousMessageDto {
    return AnonymousMessageDto(
        id = id,
        senderEmail = senderEmail,
        content = content,
        createdAt = createdAt
    )
}
