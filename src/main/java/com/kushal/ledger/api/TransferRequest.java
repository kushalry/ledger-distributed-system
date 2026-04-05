package com.kushal.ledger.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "Source wallet ID cannot be null") UUID fromWalletId,

        @NotNull(message = "Destination wallet ID cannot be null") UUID toWalletId,

        @NotNull(message = "Transfer amount cannot be null") @Positive(message = "Transfer amount must be strictly greater than zero") BigDecimal amount) {

}
