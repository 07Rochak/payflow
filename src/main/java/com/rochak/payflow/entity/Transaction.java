package com.rochak.payflow.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Transaction",
        description = """
                Internal JPA persistence model representing a wallet transaction.

                Transactions provide the persistent audit record for wallet movements such as
                transfers, deposits, withdrawals, refunds and administrator adjustments.
                Public transaction-history endpoints return TransactionResponseDTO.
                """
)public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique database identifier of the transaction.",
            example = "100",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id")
    @Schema(
            description = "Wallet from which funds are debited. Null for transaction types "
                    + "that do not have a sender wallet.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id")
    @Schema(
            description = "Wallet receiving funds. Null for transaction types that do not "
                    + "have a receiver wallet.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Wallet receiverWallet;

    @Column(nullable = false)
    @Schema(
            description = "Transaction amount in INR.",
            example = "250.0"
    )
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Business operation that generated the transaction.",
            example = "TRANSFER"
    )
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Current processing status of the transaction.",
            example = "SUCCESS"
    )
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Schema(
            description = "Accounting category describing the direction or classification of the transaction.",
            example = "DEBIT"
    )
    private TransactionCategory category;

    @Column(nullable = false)
    @Schema(
            description = "Timestamp at which the transaction record was created.",
            example = "2026-08-19T12:30:00"
    )
    private LocalDateTime createdAt;

    @Column(length = 255)
    @Schema(
            description = "Human-readable description of the transaction.",
            example = "Wallet transfer"
    )
    private String description;

    @Column(length = 100)
    @Schema(
            description = "External system reference associated with the transaction, when applicable.",
            example = "PAY_20260819_000012"
    )
    private String externalReference;
}
