package com.kushal.ledger.api;

import com.kushal.ledger.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Financial Transfers", description = "High-concurrency transfer engine")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Execute an atomic wallet transfer", 
               description = "Securely moves funds between two wallets using pessimistic locking.")
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @ApiResponse(responseCode = "400", description = "Invalid request payload or insufficient funds")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
        
        transferService.transfer(request.fromWalletId(), request.toWalletId(), request.amount());
        
        return ResponseEntity.ok("Transfer executed successfully");
    }
}