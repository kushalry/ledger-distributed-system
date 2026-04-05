package com.kushal.ledger.service;

import com.kushal.ledger.domain.User;
import com.kushal.ledger.domain.Wallet;
import com.kushal.ledger.repository.UserRepository;
import com.kushal.ledger.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class TransferServiceConcurrencyTest {

    // SDE 2 Magic: Automatically spin up a real Postgres DB just for this test
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TransferService transferService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID aliceWalletId;
    private UUID bobWalletId;

    @BeforeEach
    void setup() {
        // Clear the test database
        walletRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Alice with $1000
        User alice = new User();
        alice.setName("Alice");
        userRepository.save(alice);
        Wallet aliceWallet = new Wallet();
        aliceWallet.setUserId(alice.getId());
        aliceWallet.setBalance(new BigDecimal("1000.0000"));
        aliceWallet = walletRepository.save(aliceWallet);
        aliceWalletId = aliceWallet.getId();

        // Setup Bob with $0
        User bob = new User();
        bob.setName("Bob");
        userRepository.save(bob);
        Wallet bobWallet = new Wallet();
        bobWallet.setUserId(bob.getId());
        bobWallet.setBalance(new BigDecimal("0.0000"));
        bobWallet = walletRepository.save(bobWallet);
        bobWalletId = bobWallet.getId();
    }

    @Test
    void testConcurrentTransfers_PessimisticLocksHold() throws InterruptedException {
        int threadCount = 100;
        BigDecimal transferAmount = new BigDecimal("5.0000"); // 100 threads * $5 = $500 total

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // Wait for the green light
                    transferService.transfer(aliceWalletId, bobWalletId, transferAmount);
                } catch (Exception e) {
                    System.out.println("Transfer failed: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Green light! Release all 100 threads at the exact same time
        latch.countDown(); 
        
        // Wait for all threads to finish
        doneLatch.await(); 

        // Assert the final state
        Wallet finalAliceWallet = walletRepository.findById(aliceWalletId).orElseThrow();
        Wallet finalBobWallet = walletRepository.findById(bobWalletId).orElseThrow();

        // If the locks failed, these balances will be completely wrong due to race conditions.
        // If your code is truly SDE 2 level, Alice will have $500 and Bob will have $500.
        assertEquals(0, new BigDecimal("500.0000").compareTo(finalAliceWallet.getBalance()), "Alice's balance is wrong!");
        assertEquals(0, new BigDecimal("500.0000").compareTo(finalBobWallet.getBalance()), "Bob's balance is wrong!");
    }
}