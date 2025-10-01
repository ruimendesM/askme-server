package com.ruimendes.askme.infra.mappers

import com.ruimendes.askme.domain.models.Chat
import com.ruimendes.askme.domain.models.ChatMessage
import com.ruimendes.askme.domain.models.ChatParticipant
import com.ruimendes.askme.infra.database.entities.ChatEntity
import com.ruimendes.askme.infra.database.entities.ChatMessageEntity
import com.ruimendes.askme.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        creator = this.creator.toChatParticipant(),
        participants = this.participants.map { it.toChatParticipant() }.toSet(),
        createdAt = this.createdAt,
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        lastMessage = lastMessage
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = this.userId,
        username = this.username,
        email = this.email,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = this.userId,
        username = this.username,
        email = this.email,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id!!,
        chatId = this.chatId,
        content = this.content,
        createdAt = this.createdAt,
        sender = this.sender.toChatParticipant()
    )
}