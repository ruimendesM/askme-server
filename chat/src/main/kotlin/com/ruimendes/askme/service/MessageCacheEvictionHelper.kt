package com.ruimendes.askme.service

import com.ruimendes.askme.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class MessageCacheEvictionHelper {

    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let Spring handle the cache evict
    }
}