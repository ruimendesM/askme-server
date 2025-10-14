package com.ruimendes.askme.infra.database

import com.ruimendes.askme.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, Long> {
    fun findByUserIdIn(userIds: List<UserId>): List<DeviceTokenEntity>

    fun findByToken(token: String): DeviceTokenEntity?

    fun deleteByToken(token: String)
}