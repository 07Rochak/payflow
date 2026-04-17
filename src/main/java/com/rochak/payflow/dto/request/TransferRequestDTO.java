package com.rochak.payflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestDTO {

    @NotNull
    private Long fromUserId;

    @NotNull
    private Long toUserId;

    @NotNull
    @Min(1)
    private Double amount;
}
