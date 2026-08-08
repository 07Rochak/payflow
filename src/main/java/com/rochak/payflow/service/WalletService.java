package com.rochak.payflow.service;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.request.WithdrawRequestDto;
import com.rochak.payflow.dto.response.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO getWalletByUserId(long userId);
    WalletResponseDTO getWalletByEmail(String email);
    WalletResponseDTO addMoney(Long userId, AddMoneyRequestDTO request);
    WalletResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO);
    WalletResponseDTO withdrawMoney(String email, WithdrawRequestDto request);
    void freezeWallet(long walletId);
    void unFreezeWallet(long walletId);
    WalletResponseDTO creditWallet(Long userId, Double amount, String description, String paymentReference);
}
