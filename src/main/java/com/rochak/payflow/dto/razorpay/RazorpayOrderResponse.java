package com.rochak.payflow.dto.razorpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "RazorpayOrderResponse",
        description = """
                Internal response model returned by Razorpay when a payment order is created.

                This model represents the provider response consumed by PayFlow and is primarily
                included for developer reference.
                """
)
public class RazorpayOrderResponse {
    @Schema(
            description = "Unique Razorpay order identifier.",
            example = "order_RazorpayTest123"
    )
    private String id;
    @Schema(
            description = "Razorpay resource type.",
            example = "order"
    )
    private String entity;
    @Schema(
            description = "Order amount in the smallest currency unit. For INR this is paise.",
            example = "50000"
    )
    private Integer amount;
    @Schema(
            description = "Amount already paid against the order in the smallest currency unit.",
            example = "0"
    )
    private Integer amountPaid;
    @Schema(
            description = "Remaining amount due in the smallest currency unit.",
            example = "50000"
    )
    private Integer amountDue;
    @Schema(
            description = "Currency associated with the Razorpay order.",
            example = "INR"
    )
    private String currency;
    @Schema(
            description = "Receipt identifier supplied by PayFlow when creating the order.",
            example = "PAY_20260819_000012"
    )
    private String receipt;
    @Schema(
            description = "Current Razorpay order status.",
            example = "created"
    )
    private String status;
    @Schema(
            description = "Number of payment attempts associated with the order.",
            example = "0"
    )
    private Integer attempts;
    @Schema(
            description = "Unix timestamp representing when Razorpay created the order.",
            example = "1755606600",
            format = "int64"
    )
    private Long createdAt;
}
