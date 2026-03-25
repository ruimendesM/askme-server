package com.ruimendes.askme.infra.mappers

import com.ruimendes.askme.domain.models.AnonymousMessage
import com.ruimendes.askme.infra.database.entities.AnonymousMessageEntity

fun AnonymousMessageEntity.toAnonymousMessage(): AnonymousMessage {
    return AnonymousMessage(
        id = id!!,
        senderEmail = senderEmail,
        content = content,
        createdAt = createdAt
    )
}
