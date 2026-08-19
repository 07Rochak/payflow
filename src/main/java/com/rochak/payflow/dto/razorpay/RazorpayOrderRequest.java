package com.rochak.payflow.dto.razorpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        name = "RazorpayOrderRequest",
        description = """
                Internal request model used by PayFlow when creating a Razorpay order.

                This model represents the provider-facing request and is not a public PayFlow
                client request.
                """
)public class RazorpayOrderRequest {
    @Schema(
            description = "Order amount in the smallest currency unit. For INR this is paise.",
            example = "50000",
            minimum = "1"
    )
    private Integer amount;

    @Schema(
            description = "Currency used for the Razorpay order.",
            example = "INR",
            defaultValue = "INR"
    )
    private String currency;

    @Schema(
            description = "Unique PayFlow receipt identifier used to correlate the Razorpay order.",
            example = "PAY_20260819_000012"
    )
    private String receipt;
}
