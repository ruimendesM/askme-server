package com.ruimendes.askme.domain.exception

class RateLimitException(
    val resetInSeconds: Long
): RuntimeException("Rate limit exceeded. Try again in $resetInSeconds seconds")