package com.ruimendes.askme.domain.model

import com.ruimendes.askme.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean
)