package com.ruimendes.askme.domain.events.chat

import com.ruimendes.askme.domain.events.AskmeEvent
import com.ruimendes.askme.domain.type.ChatId
import com.ruimendes.askme.domain.type.UserId
import java.time.Instant
import java.util.*

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
) : AskmeEvent {

    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ) : ChatEvent()
}

