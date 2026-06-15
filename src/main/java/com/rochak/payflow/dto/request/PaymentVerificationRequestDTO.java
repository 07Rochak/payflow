package com.rochak.payflow.dto.request;

import com.rochak.payflow.repository.PaymentRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentVerificationRequestDTO {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;

    private final PaymentRepository paymentRepository;
}
