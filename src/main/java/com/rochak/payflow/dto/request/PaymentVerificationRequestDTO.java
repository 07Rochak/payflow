package com.rochak.payflow.dto.request;

import com.rochak.payflow.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Razorpay payment details returned by Checkout and used by PayFlow for signature verification.")
public class PaymentVerificationRequestDTO {

    @NotBlank
    @Schema(description = "Razorpay order ID returned by the create-order operation.", example = "order_RazorpayTest123")
    private String razorpayOrderId;

    @NotBlank
    @Schema(description = "Razorpay payment ID returned after successful Checkout.", example = "pay_RazorpayTest123")
    private String razorpayPaymentId;

    @NotBlank
    @Schema(description = "Razorpay signature returned by Checkout and verified by PayFlow.", example = "signature_generated_by_razorpay")
    private String razorpaySignature;

    @Schema(hidden = true)
    private final PaymentRepository paymentRepository;
}
