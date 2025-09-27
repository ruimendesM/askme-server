package com.ruimendes.askme.domain.events

import java.time.Instant

interface AskmeEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}