package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class LoginRequest @JsonCreator constructor(
    @JsonProperty("email")
    val email: String,
    @JsonProperty("password")
    val password: String
)
