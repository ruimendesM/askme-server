package com.ruimendes.askme.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest @JsonCreator constructor(
    @field:Size(min = 1, message = "Chat needs at least two unique participants")
    @JsonProperty("other_user_ids")
    val otherUserIds: List<UserId>
)