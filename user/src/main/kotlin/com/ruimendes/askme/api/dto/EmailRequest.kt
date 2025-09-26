package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email

data class EmailRequest @JsonCreator constructor(
    @field:Email
    @JsonProperty("email")
    val email: String
)
