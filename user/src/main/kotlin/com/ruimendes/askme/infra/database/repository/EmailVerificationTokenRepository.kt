package com.ruimendes.askme.infra.database.repository

import com.ruimendes.askme.infra.database.entities.EmailVerificationTokenEntity
import com.ruimendes.askme.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {

    fun findByToken(token: String): EmailVerificationTokenEntity?

    fun deleteByExpiresAtLessThan(now: Instant)

    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}