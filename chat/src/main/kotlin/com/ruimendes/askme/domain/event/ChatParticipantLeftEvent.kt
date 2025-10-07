package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
