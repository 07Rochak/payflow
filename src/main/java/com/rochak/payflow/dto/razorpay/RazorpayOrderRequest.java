package com.rochak.payflow.dto.razorpay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RazorpayOrderRequest {
    private Integer amount;
    private String currency;
    private String receipt;
}
