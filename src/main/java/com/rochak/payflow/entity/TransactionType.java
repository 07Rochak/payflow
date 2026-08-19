package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TransactionType",
        description = "Business operation represented by a wallet transaction."
)
public enum TransactionType {
    @Schema(description = "Transfer of funds between two user wallets.")
    TRANSFER,
    @Schema(description = "Money deposited into a wallet.")
    DEPOSIT,
    @Schema(description = "Money withdrawn from a wallet.")
    WITHDRAWAL,
    @Schema(description = "Refund of a previously processed transaction.")
    REFUND,
    @Schema(description = "Wallet credit initiated by an administrator.")
    ADMIN_CREDIT,
    @Schema(description = "Wallet debit initiated by an administrator.")
    ADMIN_DEBIT
}
