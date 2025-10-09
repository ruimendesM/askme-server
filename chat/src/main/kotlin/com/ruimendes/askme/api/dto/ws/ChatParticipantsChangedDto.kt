package com.ruimendes.askme.api.dto.ws

import com.ruimendes.askme.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId
)
