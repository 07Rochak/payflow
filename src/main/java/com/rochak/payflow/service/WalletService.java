package com.rochak.payflow.service;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO getWalletByUserId(long userId);
    WalletResponseDTO addMoney(Long userId, AddMoneyRequestDTO request);
}
