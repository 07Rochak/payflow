package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PaymentFailureReason",
        description = "Reason recorded when PayFlow payment processing fails."
)
public enum PaymentFailureReason {
    @Schema(description = "No failure has been recorded.")
    NONE,
    @Schema(description = "Razorpay payment signature validation failed.")
    INVALID_SIGNATURE,
    @Schema(description = "Payment was verified but wallet credit could not be completed.")
    WALLET_CREDIT_FAILED,
    @Schema(description = "Wallet credit would exceed the configured wallet limit.")
    WALLET_LIMIT_EXCEEDED,
    @Schema(description = "Transaction record creation failed.")
    TRANSACTION_CREATION_FAILED,
    @Schema(description = "Communication with the Razorpay API failed.")
    RAZORPAY_API_ERROR,
    @Schema(description = "A database operation failed during payment processing.")
    DATABASE_ERROR,
    @Schema(description = "Failure occurred but no more specific reason was available.")
    UNKNOWN
}
