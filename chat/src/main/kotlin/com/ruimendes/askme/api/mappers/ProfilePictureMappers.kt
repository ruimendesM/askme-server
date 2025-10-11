package com.ruimendes.askme.api.mappers

import com.ruimendes.askme.api.dto.PictureUploadResponse
import com.ruimendes.askme.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse() = PictureUploadResponse(
    uploadUrl = uploadUrl,
    publicUrl = publicUrl,
    headers = headers,
    expiresAt = expiresAt
)