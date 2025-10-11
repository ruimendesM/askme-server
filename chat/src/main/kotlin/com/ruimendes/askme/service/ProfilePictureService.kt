package com.ruimendes.askme.service

import com.ruimendes.askme.domain.event.ProfilePictureUpdatedEvent
import com.ruimendes.askme.domain.exception.ChatParticipantNotFoundException
import com.ruimendes.askme.domain.exception.InvalidProfilePictureException
import com.ruimendes.askme.domain.models.ProfilePictureUploadCredentials
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.repositories.ChatParticipantRepository
import com.ruimendes.askme.infra.storage.SupabaseStorageService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProfilePictureService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

    fun generateUploadCredentials(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
        return supabaseStorageService.generateSignedUploadUrl(userId, mimeType)
    }

    @Transactional
    fun deleteProfilePicture(userId: UserId) {
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        participant.profilePictureUrl?.let { url ->

            chatParticipantRepository.save(
                participant.apply { profilePictureUrl = null }
            )

            supabaseStorageService.deleteFile(url)

            applicationEventPublisher.publishEvent(
                ProfilePictureUpdatedEvent(
                    userId = userId,
                    newUrl = null
                )
            )
        }
    }

    @Transactional
    fun confirmProfilePictureUpload(userId: UserId, publicUrl: String) {
        if (!publicUrl.startsWith(supabaseUrl)) {
            throw InvalidProfilePictureException("Invalid profile picture URL")
        }

        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        val oldUrl = participant.profilePictureUrl

        chatParticipantRepository.save(
            participant.apply { profilePictureUrl = publicUrl }
        )

        try {
            oldUrl?.let {
                supabaseStorageService.deleteFile(it)
            }
        } catch(e: Exception) {
            logger.warn("Deleting old profile picture failed for user $userId", e)
        }

        applicationEventPublisher.publishEvent(
            ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}