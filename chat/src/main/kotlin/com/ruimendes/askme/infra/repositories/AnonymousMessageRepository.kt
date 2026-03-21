package com.ruimendes.askme.infra.repositories

import com.ruimendes.askme.domain.type.AnonymousMessageId
import com.ruimendes.askme.infra.database.entities.AnonymousMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface AnonymousMessageRepository : JpaRepository<AnonymousMessageEntity, AnonymousMessageId> {

    @Query(
        """
            SELECT m
            FROM AnonymousMessageEntity m
            WHERE m.createdAt < :before
            ORDER BY m.createdAt DESC
        """
    )
    fun findByCreatedAtBefore(before: Instant, pageable: Pageable): Slice<AnonymousMessageEntity>
}
