package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.AnonymousMessageId

data class AnonymousMessageReceivedEvent(
    val messageId: AnonymousMessageId
)
