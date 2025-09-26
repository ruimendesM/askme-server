package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.api.util.Password
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length

data class RegisterRequest @JsonCreator constructor(
    @field:Email(message = "Must be a valid email address")
    @JsonProperty("email")
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @JsonProperty("username")
    val username: String,
    @field:Password
    @JsonProperty("password")
    val password: String
)