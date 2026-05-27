package com.rochak.payflow.dto.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponseDTO {
    private String orderId;
    private Integer amount;
    private String currency;
}
