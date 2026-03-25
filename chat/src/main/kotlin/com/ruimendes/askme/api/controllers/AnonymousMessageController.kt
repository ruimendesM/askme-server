package com.ruimendes.askme.api.controllers

import com.ruimendes.askme.api.config.IpRateLimit
import com.ruimendes.askme.api.dto.AnonymousMessageDto
import com.ruimendes.askme.api.dto.CreateAnonymousMessageRequest
import com.ruimendes.askme.api.mappers.toAnonymousMessageDto
import com.ruimendes.askme.service.AnonymousMessageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/anonymous-messages")
class AnonymousMessageController(
    private val anonymousMessageService: AnonymousMessageService
) {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @IpRateLimit(requests = 5, duration = 10, unit = TimeUnit.MINUTES)
    fun sendMessage(@Valid @RequestBody request: CreateAnonymousMessageRequest) {
        anonymousMessageService.sendMessage(
            senderEmail = request.senderEmail,
            content = request.content
        )
    }

    @GetMapping
    fun getMessages(
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<AnonymousMessageDto> {
        return anonymousMessageService.getMessages(before, pageSize)
            .map { it.toAnonymousMessageDto() }
    }
}
