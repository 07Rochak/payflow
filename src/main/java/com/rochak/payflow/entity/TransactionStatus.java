package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TransactionStatus",
        description = "Processing status of a wallet transaction."
)
public enum TransactionStatus {
    @Schema(description = "Transaction has been created but processing is not complete.")
    PENDING,
    @Schema(description = "Transaction completed successfully.")
    SUCCESS,
    @Schema(description = "Transaction processing failed.")
    FAILED,
    @Schema(description = "Previously completed transaction was reversed.")
    REVERSED
}
