package com.ruimendes.askme.service

import com.ruimendes.askme.domain.exception.ChatParticipantNotFoundException
import com.ruimendes.askme.domain.exception.InvalidChatSizeException
import com.ruimendes.askme.domain.models.Chat
import com.ruimendes.askme.domain.type.UserId
import com.ruimendes.askme.infra.database.entities.ChatEntity
import com.ruimendes.askme.infra.mappers.toChat
import com.ruimendes.askme.infra.repositories.ChatParticipantRepository
import com.ruimendes.askme.infra.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {
        if (otherUserIds.isEmpty()) {
            throw InvalidChatSizeException()
        }
        val otherParticipants = chatParticipantRepository.findByUserIdIn(otherUserIds)

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = mutableSetOf(creator).apply { addAll(otherParticipants) }
            )
        ).toChat()
    }
}