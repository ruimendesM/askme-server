package com.ruimendes.askme.api.mappers

import com.ruimendes.askme.api.dto.AuthenticatedUserDto
import com.ruimendes.askme.api.dto.UserDto
import com.ruimendes.askme.domain.model.AuthenticatedUser
import com.ruimendes.askme.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto() = AuthenticatedUserDto(
    user = user.toUserDto(),
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun User.toUserDto() = UserDto(
    id = id,
    email = email,
    username = username,
    hasEmailVerified = hasEmailVerified
)