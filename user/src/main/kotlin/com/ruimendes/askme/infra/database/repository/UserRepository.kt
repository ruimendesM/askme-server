package com.ruimendes.askme.infra.database.repository

import com.ruimendes.askme.domain.model.UserId
import com.ruimendes.askme.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?

    fun findByEmailOrUsername(email: String, username: String): UserEntity?

    
}