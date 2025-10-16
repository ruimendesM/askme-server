package com.ruimendes.askme.api.controllers

import com.ruimendes.askme.api.dto.DeviceTokenDto
import com.ruimendes.askme.api.dto.RegisterDeviceRequest
import com.ruimendes.askme.api.mappers.toDeviceTokenDto
import com.ruimendes.askme.api.mappers.toPlatform
import com.ruimendes.askme.api.util.requestUserId
import com.ruimendes.askme.infra.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(private val pushNotificationService: PushNotificationService) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String
    ) {
        pushNotificationService.unregisterDevice(token)
    }

}