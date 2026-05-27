package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.mapper.WalletMapper;
import com.rochak.payflow.repository.TransactionRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {
    private WalletRepository walletRepository;
    private UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public WalletResponseDTO getWalletByUserId(long userId) {
        System.out.println("process reached");
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        System.out.println("wallet extracted");
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    public WalletResponseDTO getWalletByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        Wallet wallet = walletRepository.findByUser_Id(user.getId())
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
    public WalletResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO) {
//        Wallet first, second;
//        if (transferRequestDTO.getFromUserId() < transferRequestDTO.getToUserId()) {
//            first = walletRepository.findByUser_Id(transferRequestDTO.getFromUserId()).orElseThrow(
//                    ()-> new ResourceNotFoundException("Wallet not found")
//            );
//            second = walletRepository.findByUser_Id(transferRequestDTO.getToUserId()).orElseThrow(
//                    ()-> new ResourceNotFoundException("Wallet not found")
//            );
//        } else {
//            first = walletRepository.findByUser_Id(transferRequestDTO.getToUserId()).orElseThrow(
//                    ()-> new ResourceNotFoundException("Wallet not found")
//            );
//            second = walletRepository.findByUser_Id(transferRequestDTO.getFromUserId()).orElseThrow(
//                    ()-> new ResourceNotFoundException("Wallet not found")
//            );
//        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        Wallet sender = walletRepository.findByUser_Id(user.getId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );

        Wallet receiver = walletRepository.findByUser_Id(transferRequestDTO.getToUserId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );

        if(user.getId().equals(transferRequestDTO.getToUserId())){
            throw new RuntimeException("Cannot transfer to same user");
        }

        if(sender.getBalance()< transferRequestDTO.getAmount()){
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - transferRequestDTO.getAmount());
        receiver.setBalance(receiver.getBalance()+ transferRequestDTO.getAmount());
        Wallet savedSender = walletRepository.save(sender);
        walletRepository.save(receiver);
        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .amount(transferRequestDTO.getAmount())
                .transactionType("TRANSFER")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        return WalletMapper.mapToResponse(savedSender);
    }
}
