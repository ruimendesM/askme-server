package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantsIds: List<UserId>
)
