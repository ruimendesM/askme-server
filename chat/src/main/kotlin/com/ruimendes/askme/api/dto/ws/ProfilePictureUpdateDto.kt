package com.ruimendes.askme.api.dto.ws

import com.ruimendes.askme.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)