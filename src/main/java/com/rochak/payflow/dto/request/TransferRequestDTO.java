package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Wallet transfer request. The sender is always the authenticated user.")
public class TransferRequestDTO {
//
//    @NotNull
//    private Long fromUserId;

    @NotNull(message = "Reiever user id required")
    @Schema(description = "Database ID of the user receiving the transfer.", example = "2", minimum = "1")
    private Long toUserId;

    @NotNull(message = "Amount required")
    @Min(value = 1, message = "Amount must be greater than 0")
    @Schema(description = "Transfer amount in INR.", example = "250.0", minimum = "1")
    private Double amount;
}
