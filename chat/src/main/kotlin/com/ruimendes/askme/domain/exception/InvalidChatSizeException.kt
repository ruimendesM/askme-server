package com.ruimendes.askme.domain.exception

import java.lang.RuntimeException

class InvalidChatSizeException: RuntimeException("There must be at least 2 unique participants in a chat")