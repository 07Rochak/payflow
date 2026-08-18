package com.rochak.payflow.dto.razorpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Internal Razorpay order response received from the payment provider.")
public class RazorpayOrderResponse {
    private String id;
    private String entity;
    private Integer amount;
    private Integer amountPaid;
    private Integer amountDue;
    private String currency;
    private String receipt;
    private String status;
    private Integer attempts;
    private Long createdAt;
}
