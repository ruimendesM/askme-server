package com.ruimendes.askme.domain.exception

class InvalidTokenException(
    override val message: String?,
): RuntimeException(message ?: "Invalid token")