package com.rochak.payflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Wallet transaction record.")
public class TransactionResponseDTO {
    @Schema(description = "Transaction database ID.", example = "100")
    private Long transactionId;

    @Schema(description = "Sender wallet ID, when applicable.", example = "10")
    private Long senderWalletId;

    @Schema(description = "Receiver wallet ID, when applicable.", example = "11")
    private Long receiverWalletId;

    @Schema(description = "Transaction amount in INR.", example = "250.0")
    private Double amount;

    @Schema(description = "Transaction type.", example = "TRANSFER")
    private String transactionType;

    @Schema(description = "Transaction status.", example = "SUCCESS")
    private String status;

    @Schema(description = "Transaction creation timestamp.", example = "2026-08-19T12:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Transaction category.", example = "DEBIT")
    private String category;

    @Schema(description = "Human-readable transaction description.", example = "Wallet transfer")
    private String description;
}
