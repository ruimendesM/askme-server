package com.ruimendes.askme.api.dto

import com.ruimendes.askme.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
