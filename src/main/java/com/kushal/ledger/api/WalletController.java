package com.kushal.ledger.api;

import com.kushal.ledger.domain.Wallet;
import com.kushal.ledger.repository.WalletRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallet Management", description = "Check wallet balances")
public class WalletController {

    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<Wallet> getWallet(@PathVariable UUID id) {
        return walletRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}