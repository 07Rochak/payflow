package com.rochak.payflow.mapper;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.entity.Transaction;

public class TransactionMapper {
    public static TransactionResponseDTO mapToResponse(Transaction transaction){
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .senderWalletId(transaction.getSenderWallet() != null ? transaction.getSenderWallet().getId() : null)
                .receiverWalletId(transaction.getReceiverWallet() != null ? transaction.getReceiverWallet().getId() : null)
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .status(transaction.getStatus().name())
                .category(transaction.getCategory() != null ? transaction.getCategory().name() : null)
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
