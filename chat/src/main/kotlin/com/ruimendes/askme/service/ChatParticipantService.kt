package com.ruimendes.askme.service

import com.ruimendes.askme.domain.models.ChatParticipant
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.mappers.toChatParticipant
import com.ruimendes.askme.infra.mappers.toChatParticipantEntity
import com.ruimendes.askme.infra.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(normalizedQuery)?.toChatParticipant()
    }
}