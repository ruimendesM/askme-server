package com.ruimendes.askme.api.controllers

import com.ruimendes.askme.api.dto.ChatDto
import com.ruimendes.askme.api.dto.CreateChatRequest
import com.ruimendes.askme.api.mappers.toChatDto
import com.ruimendes.askme.api.util.requestUserId
import com.ruimendes.askme.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}