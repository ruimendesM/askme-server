package com.ruimendes.askme.domain.models

import com.ruimendes.askme.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
