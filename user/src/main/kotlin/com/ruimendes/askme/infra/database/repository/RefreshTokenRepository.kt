package com.ruimendes.askme.infra.database.repository

import com.ruimendes.askme.domain.model.UserId
import com.ruimendes.askme.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(userId: UserId, hashedToken: String): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(userId: UserId, hashedToken: String)

    fun deleteByUserId(userId: UserId)
}