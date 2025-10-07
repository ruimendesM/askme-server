package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)
