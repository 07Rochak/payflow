package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PaymentStatus",
        description = "Lifecycle status of a PayFlow payment."
)
public enum PaymentStatus {
    @Schema(description = "Payment record has been created.")
    CREATED,
    @Schema(description = "Payment order exists and is awaiting successful payment/verification.")
    PENDING,
    @Schema(description = "Razorpay payment was successfully verified and processing completed.")
    SUCCESS,
    @Schema(description = "Payment processing or verification failed.")
    FAILED,
    @Schema(description = "Payment expired before successful completion.")
    EXPIRED,
    @Schema(description = "Payment was cancelled.")
    CANCELLED
}
