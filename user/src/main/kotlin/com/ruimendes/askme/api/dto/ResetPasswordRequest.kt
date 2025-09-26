package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("token")
    val token: String,
    @field:Password
    @JsonProperty("new_password")
    val newPassword: String
)