package com.ruimendes.askme.infra.database.mappers

import com.ruimendes.askme.domain.model.EmailVerificationToken
import com.ruimendes.askme.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken() =
    EmailVerificationToken(
        id = id!!,
        token = token,
        user = user.toUser()
    )