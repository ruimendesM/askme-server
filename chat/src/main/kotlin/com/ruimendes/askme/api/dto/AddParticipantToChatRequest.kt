package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatRequest @JsonCreator constructor(
    @JsonProperty("user_ids")
    @field:Size(min = 1, message = "user_ids must have at least one user ID")
    val userIds: List<UserId>,
)