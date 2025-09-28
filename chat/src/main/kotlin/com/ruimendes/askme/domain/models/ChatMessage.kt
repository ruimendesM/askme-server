package com.ruimendes.askme.domain.models

import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant
)