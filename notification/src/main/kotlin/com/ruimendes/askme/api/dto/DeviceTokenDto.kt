package com.ruimendes.askme.api.dto

import com.ruimendes.askme.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)
