package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("old_password")
    val oldPassword: String,
    @field:Password
    @JsonProperty("new_password")
    val newPassword: String
)