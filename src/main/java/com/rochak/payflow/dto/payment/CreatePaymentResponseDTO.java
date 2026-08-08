package com.rochak.payflow.dto.payment;

import com.rochak.payflow.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponseDTO {
    private Long paymentId;
    private String orderId;
    private Integer amount;
    private String currency;
    private PaymentStatus status;
}
