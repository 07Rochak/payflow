package com.rochak.payflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDTO {
    private Long transactionId;
    private Long senderWalletId;
    private Long receiverWalletId;
    private Double amount;
    private String transactionType;
    private String status;
    private LocalDateTime createdAt;
}
