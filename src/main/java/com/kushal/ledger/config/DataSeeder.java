package com.kushal.ledger.config;

import com.kushal.ledger.domain.User;
import com.kushal.ledger.domain.Wallet;
import com.kushal.ledger.repository.UserRepository;
import com.kushal.ledger.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public DataSeeder(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed data if the database is empty
        if (walletRepository.count() == 0) {
            
            User alice = new User();
            alice.setName("Alice");
            userRepository.save(alice);

            User bob = new User();
            bob.setName("Bob");
            userRepository.save(bob);

            Wallet aliceWallet = new Wallet();
            aliceWallet.setUserId(alice.getId());
            aliceWallet.setBalance(new BigDecimal("1000.0000")); // Alice starts with $1000
            walletRepository.save(aliceWallet);

            Wallet bobWallet = new Wallet();
            bobWallet.setUserId(bob.getId());
            bobWallet.setBalance(new BigDecimal("500.0000")); // Bob starts with $500
            walletRepository.save(bobWallet);

            System.out.println("\n=========================================");
            System.out.println("🏦 TEST DATA GENERATED SUCCESSFULLY!");
            System.out.println("Alice's Wallet ID (Balance $1000): " + aliceWallet.getId());
            System.out.println("Bob's Wallet ID (Balance $500): " + bobWallet.getId());
            System.out.println("=========================================\n");
        }
    }
}