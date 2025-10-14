package com.ruimendes.askme.infra.mappers

import com.ruimendes.askme.domain.model.DeviceToken
import com.ruimendes.askme.infra.database.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        id = this.id,
        userId = this.userId,
        token = this.token,
        platform = this.platform.toPlatform(),
        createdAt = this.createdAt
    )
}