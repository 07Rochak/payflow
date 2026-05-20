package com.rochak.payflow.service;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO getWalletByUserId(long userId);
    WalletResponseDTO getWalletByEmail(String email);
    WalletResponseDTO addMoney(Long userId, AddMoneyRequestDTO request);
    WalletResponseDTO transferMoney(TransferRequestDTO transferRequestDTO);
}
