package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.ChatMessageId
import com.ruimendes.askme.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
