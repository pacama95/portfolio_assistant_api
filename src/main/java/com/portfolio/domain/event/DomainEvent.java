package com.portfolio.domain.event;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent<T> {
    private final UUID eventId;
    private final Instant occurredAt;
    private final T data;

    protected DomainEvent(T data) {
        this.data = data;
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public T getData() { return data; }
}
