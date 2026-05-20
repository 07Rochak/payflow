package com.rochak.payflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestDTO {
//
//    @NotNull
//    private Long fromUserId;

    @NotNull(message = "Reiever user id required")
    private Long toUserId;

    @NotNull(message = "Amount required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Double amount;
}
