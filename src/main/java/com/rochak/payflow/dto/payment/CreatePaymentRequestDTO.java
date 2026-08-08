package com.rochak.payflow.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequestDTO {
    @NotNull
    @Min(1)
    private Double amount;
}
