package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshRequest @JsonCreator constructor(
    @JsonProperty("refresh_token")
    val refreshToken: String,
)
