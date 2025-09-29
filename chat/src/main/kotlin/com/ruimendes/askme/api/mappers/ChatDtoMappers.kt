package com.ruimendes.askme.api.mappers

import com.ruimendes.askme.api.dto.ChatDto
import com.ruimendes.askme.api.dto.ChatMessageDto
import com.ruimendes.askme.api.dto.ChatParticipantDto
import com.ruimendes.askme.domain.models.Chat
import com.ruimendes.askme.domain.models.ChatMessage
import com.ruimendes.askme.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map { it.toChatParticipantDto() },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto():  ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}