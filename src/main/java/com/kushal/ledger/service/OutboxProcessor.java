package com.kushal.ledger.service;

import com.kushal.ledger.domain.OutboxEvent;
import com.kushal.ledger.repository.OutboxRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;

    private final KafkaTemplate<String, String> kafkaTemplate; // In Phase 3, we will inject this to publish to Kafka

    public OutboxProcessor(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Wake up every 5 seconds (5000 milliseconds)
    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        // 1. Fetch all pending events from the database
        List<OutboxEvent> events = outboxRepository.findAll();

        if (events.isEmpty()) {
            return; // Go back to sleep if nothing is there
        }

        System.out.println("📦 Found " + events.size() + " pending outbox events. Processing...");

        for (OutboxEvent event : events) {
            try {
                // 2. SIMULATE PUBLISHING TO KAFKA / RABBITMQ
                publishToMessageBroker(event);

                // 3. Delete the event so we don't process it again
                outboxRepository.delete(event);
                
            } catch (Exception e) {
                // If publishing fails, we don't delete the event. 
                // The worker will try again on the next loop!
                System.err.println("Failed to process event " + event.getId());
            }
        }
    }

    private void publishToMessageBroker(OutboxEvent event) {
        // In Phase 3, we would actually write Kafka template code here.
        // For tonight, we prove the architecture works by logging it.
        kafkaTemplate.send("ledger-events", event.getPayload()); // Simulate sending to Kafka
    }
}