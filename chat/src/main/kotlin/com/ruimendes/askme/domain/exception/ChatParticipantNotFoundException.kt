package com.ruimendes.askme.domain.exception

import com.ruimendes.askme.domain.type.UserId

class ChatParticipantNotFoundException(private val id: UserId) :
    RuntimeException("Chat participant with id $id not found")