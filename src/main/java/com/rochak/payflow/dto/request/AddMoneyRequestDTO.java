package com.rochak.payflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMoneyRequestDTO {
    @NotNull
    @Min(value = 1, message = "amount must be greater than 0")
    private Double amount;
}
