package com.ruimendes.askme.api.exception_handling

import com.ruimendes.askme.domain.exception.ChatMessageNotFoundException
import com.ruimendes.askme.domain.exception.ChatNotFoundException
import com.ruimendes.askme.domain.exception.ChatParticipantNotFoundException
import com.ruimendes.askme.domain.exception.ForbiddenException
import com.ruimendes.askme.domain.exception.InvalidChatSizeException
import com.ruimendes.askme.domain.exception.InvalidProfilePictureException
import com.ruimendes.askme.domain.exception.StorageException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        ChatMessageNotFoundException::class,
        ChatParticipantNotFoundException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(e: ForbiddenException) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class,)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSizeException(e: InvalidChatSizeException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )

    @ExceptionHandler(InvalidProfilePictureException::class,)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePictureException(e: InvalidProfilePictureException) = mapOf(
        "code" to "INVALID_PROFILE_PICTURE",
        "message" to e.message
    )

    @ExceptionHandler(StorageException::class,)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStoreException(e: StorageException) = mapOf(
        "code" to "STORAGE_ERROR",
        "message" to e.message
    )
}