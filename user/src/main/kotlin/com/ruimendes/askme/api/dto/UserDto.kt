package com.ruimendes.askme.api.dto

import com.ruimendes.askme.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasEmailVerified: Boolean
)
