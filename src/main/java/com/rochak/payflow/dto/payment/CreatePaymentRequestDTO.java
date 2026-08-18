package com.rochak.payflow.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for creating a PayFlow payment and Razorpay order.")
public class CreatePaymentRequestDTO {
    @NotNull
    @Min(1)
    @Schema(description = "Payment amount in INR. PayFlow converts the amount to paise for the Razorpay order.", example = "500.0", minimum = "1")
    private Double amount;
}
