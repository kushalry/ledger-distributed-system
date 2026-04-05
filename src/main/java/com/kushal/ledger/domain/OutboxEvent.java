package com.kushal.ledger.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // What kind of event is this? (e.g., "TRANSFER_COMPLETED")
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    // The ID of the wallet or transfer
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    // The actual JSON payload we will send to Kafka later
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    public OutboxEvent() {}

    public OutboxEvent(String aggregateType, UUID aggregateId, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
    }
}