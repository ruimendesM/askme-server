package com.ruimendes.askme.infra.database.mappers

import com.ruimendes.askme.domain.model.User
import com.ruimendes.askme.infra.database.entities.UserEntity

fun UserEntity.toUser() = User(
    id = id!!,
    username = username,
    email = email,
    hasEmailVerified = hasVerifiedEmail
)