package com.ruimendes.askme.domain.event

import com.ruimendes.askme.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)
