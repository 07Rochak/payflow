package com.rochak.payflow.dto.razorpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Internal Razorpay order request. Amount is represented in paise.")
public class RazorpayOrderRequest {
    @Schema(description = "Amount in paise", example = "10000")
    private Integer amount;

    @Schema(description = "Currently INR is used with razorpay interface")
    private String currency;

    @Schema(description = "Unique recept id for identification of order", example = "PAY_123456...")
    private String receipt;
}
