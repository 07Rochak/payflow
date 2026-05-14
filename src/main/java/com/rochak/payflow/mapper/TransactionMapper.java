package com.rochak.payflow.mapper;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.entity.Transaction;

public class TransactionMapper {
    public static TransactionResponseDTO mapToResponse(Transaction transaction){
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .senderWalletId(transaction.getSenderWallet().getId())
                .receiverWalletId(transaction.getReceiverWallet().getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
