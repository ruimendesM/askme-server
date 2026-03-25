package com.ruimendes.askme.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAnonymousMessageRequest(
    @field:Email
    @field:Size(max = 320)
    val senderEmail: String,

    @field:NotBlank
    @field:Size(max = 2000)
    val content: String
)
