package com.ruimendes.askme.infra.storage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ruimendes.askme.domain.exception.InvalidProfilePictureException
import com.ruimendes.askme.domain.exception.StorageException
import com.ruimendes.askme.domain.models.ProfilePictureUploadCredentials
import com.ruimendes.askme.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClient: RestClient,
) {
    companion object {
        private val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp"
        )

        private const val UPLOAD_EXPIRES_IN_SECONDS = 300 // 5 minutes
    }

    fun generateSignedUploadUrl(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
        val extension = allowedMimeTypes[mimeType]
            ?: throw InvalidProfilePictureException("Unsupported mime type: $mimeType")

        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = "profile-pictures/$fileName"

        val publicUrl = "$supabaseUrl/storage/v1/object/public/$path"

        return ProfilePictureUploadCredentials(
            uploadUrl = createSignedUrl(path),
            publicUrl = publicUrl,
            headers = mapOf(
                "Content-Type" to mimeType
            ),
            expiresAt = Instant.now().plusSeconds(UPLOAD_EXPIRES_IN_SECONDS.toLong())
        )
    }

    fun deleteFile(url: String) {
        val path = if (url.contains("/object/public/")) {
            url.substringAfter("/object/public/")
        } else throw StorageException("Invalid file URL format")

        val deleteUrl = "/storage/v1/object/$path"

        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if (response.statusCode.isError) {
            throw StorageException("Unable to delete file: ${response.statusCode.value()}")
        }
    }

    private fun createSignedUrl(path: String): String {
        val json = """
            {
              "expiresIn": $UPLOAD_EXPIRES_IN_SECONDS
            }
        """.trimIndent()

        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(SignedUrlResponse::class.java)
            ?: throw RuntimeException("Failed to get create signed URL")

        return "${supabaseUrl}/storage/v1${response.url}"
    }

    private data class SignedUrlResponse @JsonCreator constructor(
        @JsonProperty("url") val url: String
    )
}