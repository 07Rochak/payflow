package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.mapper.WalletMapper;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.WalletService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public WalletResponseDTO addMoney(Long userId, AddMoneyRequestDTO request) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponseDTO transferMoney(TransferRequestDTO transferRequestDTO) {
        Wallet first, second;
        if (transferRequestDTO.getFromUserId() < transferRequestDTO.getToUserId()) {
            first = walletRepository.findByUser_Id(transferRequestDTO.getFromUserId()).orElseThrow(
                    ()-> new ResourceNotFoundException("Wallet not found")
            );
            second = walletRepository.findByUser_Id(transferRequestDTO.getToUserId()).orElseThrow(
                    ()-> new ResourceNotFoundException("Wallet not found")
            );
        } else {
            first = walletRepository.findByUser_Id(transferRequestDTO.getToUserId()).orElseThrow(
                    ()-> new ResourceNotFoundException("Wallet not found")
            );
            second = walletRepository.findByUser_Id(transferRequestDTO.getFromUserId()).orElseThrow(
                    ()-> new ResourceNotFoundException("Wallet not found")
            );
        }
        Wallet sender = walletRepository.findByUser_Id(transferRequestDTO.getFromUserId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );

        Wallet receiver = walletRepository.findByUser_Id(transferRequestDTO.getToUserId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );

        if(transferRequestDTO.getFromUserId().equals(transferRequestDTO.getToUserId())){
            throw new RuntimeException("Cannot transfer to same user");
        }

        if(sender.getBalance()< transferRequestDTO.getAmount()){
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - transferRequestDTO.getAmount());
        receiver.setBalance(receiver.getBalance()+ transferRequestDTO.getAmount());
        Wallet savedSender = walletRepository.save(sender);
        walletRepository.save(receiver);
        return WalletMapper.mapToResponse(savedSender);
    }
}
