package com.rochak.payflow.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequestDTO {
    @NotNull
    @Min(1)
    private double amount;
}
