package com.ruimendes.askme.api.util

import com.ruimendes.askme.domain.exception.UnauthorizedException
import com.ruimendes.askme.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()