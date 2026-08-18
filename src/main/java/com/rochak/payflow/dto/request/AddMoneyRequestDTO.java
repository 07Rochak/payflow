package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request for adding money to a wallet. This DTO is retained for internal wallet operations.")
public class AddMoneyRequestDTO {
    @NotNull
    @Min(value = 1, message = "amount must be greater than 0")
    @Schema(description = "Amount in INR.", example = "500.0", minimum = "1")
    private Double amount;
}
