package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TransactionCategory",
        description = "Accounting direction/category assigned to a wallet transaction."
)
public enum TransactionCategory {
    @Schema(description = "Funds were credited to the relevant wallet.")
    CREDIT,
    @Schema(description = "Funds were debited from the relevant wallet.")
    DEBIT,
    @Schema(description = "Transaction represents movement of funds between wallets.")
    TRANSFER
}
