package com.rochak.payflow.service;

import com.rochak.payflow.dto.response.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionResponseDTO> getTransactionByUserId(String email);
}
