package com.kushal.ledger.service;

import com.kushal.ledger.domain.OutboxEvent;
import com.kushal.ledger.domain.Wallet;
import com.kushal.ledger.repository.OutboxRepository;
import com.kushal.ledger.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;

    private final OutboxRepository outboxRepository; // Add this line to inject the OutboxRepository

    // Spring Boot automatically injects the repository here when the app starts
    public TransferService(WalletRepository walletRepository, OutboxRepository outboxRepository) {
        this.walletRepository = walletRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        
        // 1. Prevent Deadlocks: Always lock the smaller UUID first
        UUID firstLockId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        UUID secondLockId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        // 2. Acquire Pessimistic Locks in strict order
        walletRepository.findByIdForUpdate(firstLockId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + firstLockId));
        walletRepository.findByIdForUpdate(secondLockId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + secondLockId));

        // 3. Get the actual sender and receiver references (Locks are actively held)
        Wallet sender = walletRepository.findById(fromWalletId).get();
        Wallet receiver = walletRepository.findById(toWalletId).get();

        // 4. Execute the Rich Domain Logic
        sender.debit(amount);
        receiver.credit(amount);

        //5. Create an Outbox Event for the transfer completion
        OutboxEvent event = new OutboxEvent(
            "TRANSFER_COMPLETED",
            fromWalletId, // or you could use a transfer ID if you have one
            String.format("{\"fromWalletId\":\"%s\",\"toWalletId\":\"%s\",\"amount\":%s}", fromWalletId, toWalletId, amount)
        );

        outboxRepository.save(event); // Save the event to the outbox table

        // 6. Save the updated state to the database
        walletRepository.save(sender);
        walletRepository.save(receiver);
    }
}