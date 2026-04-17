package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.mapper.WalletMapper;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {
    private WalletRepository walletRepository;

    @Override
    public WalletResponseDTO getWalletByUserId(long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    public WalletResponseDTO addMoney(Long userId, AddMoneyRequestDTO request) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);
        return WalletMapper.mapToResponse(wallet);
    }
}
