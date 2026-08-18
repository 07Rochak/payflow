package com.rochak.payflow.dto.payment;

import com.rochak.payflow.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "PayFlow payment and Razorpay order details returned after order creation.")
public class CreatePaymentResponseDTO {
    @Schema(description = "PayFlow payment database ID.", example = "12")
    private Long paymentId;

    @Schema(description = "Razorpay order ID used to initialize Checkout.", example = "order_RazorpayTest123")
    private String orderId;

    @Schema(description = "Order amount in the smallest currency unit returned by Razorpay (paise for INR).", example = "50000")
    private Integer amount;

    @Schema(description = "Payment currency.", example = "INR")
    private String currency;

    @Schema(description = "Current PayFlow payment status.", example = "PENDING")
    private PaymentStatus status;
}
