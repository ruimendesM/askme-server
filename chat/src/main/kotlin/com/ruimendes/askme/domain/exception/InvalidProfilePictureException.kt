package com.ruimendes.askme.domain.exception

class InvalidProfilePictureException(override val message: String?) :
    RuntimeException(message ?: "Invalid profile picture data")