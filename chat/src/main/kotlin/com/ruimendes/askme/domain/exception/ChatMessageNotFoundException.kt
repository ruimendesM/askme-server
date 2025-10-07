package com.ruimendes.askme.domain.exception

import com.ruimendes.askme.domain.type.ChatMessageId

class ChatMessageNotFoundException(private val id: ChatMessageId) : RuntimeException("Chat message with $id not found")