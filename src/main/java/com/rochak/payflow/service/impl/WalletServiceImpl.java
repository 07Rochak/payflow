package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.request.WithdrawRequestDto;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.*;
import com.rochak.payflow.exception.InsufficientBalanceException;
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
        Transaction transaction = Transaction.builder()
                .receiverWallet(wallet)
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .category(TransactionCategory.CREDIT)
                .description("Adding money to wallet")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        walletRepository.save(wallet);
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );

        Long senderUserId = user.getId();
        Long receiverUserId = transferRequestDTO.getToUserId();

        // locking system
        Wallet first;
        Wallet second;

        if(senderUserId<receiverUserId){
            first = walletRepository
                    .findUserIdForUpdate(senderUserId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Wallet not found")
                    );
            second = walletRepository
                    .findUserIdForUpdate(receiverUserId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Wallet not found")
                    );
        }
        else {
            first = walletRepository
                    .findUserIdForUpdate(receiverUserId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Wallet not found")
                    );
            second =walletRepository
                    .findUserIdForUpdate(senderUserId)
                    .orElseThrow(
                            ()-> new ResourceNotFoundException("Wallet not found")
                    );
        }

        Wallet sender = first.getUser().getId().equals(senderUserId) ? first : second;

        Wallet  receiver = second.getUser().getId().equals(receiverUserId) ? second : first;

//        Wallet sender = walletRepository.findByUser_Id(user.getId())
//                .orElseThrow(
//                        ()-> new ResourceNotFoundException("Wallet not found")
//                );
//
//        Wallet receiver = walletRepository.findByUser_Id(transferRequestDTO.getToUserId())
//                .orElseThrow(
//                        ()-> new ResourceNotFoundException("Wallet not found")
//                );

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
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .category(TransactionCategory.TRANSFER)
                .description("Transfer from "+sender.getUser().getEmail()+" to "+receiver.getUser().getEmail())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        return WalletMapper.mapToResponse(savedSender);
    }

    @Override
    @Transactional
    public WalletResponseDTO withdrawMoney(String email, WithdrawRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User not found")
                );

        Long id = user.getId();

        Wallet wallet = walletRepository.findByUser_Id(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Wallet not found")
                );

        if(wallet.getBalance().compareTo(request.getAmount())<0){
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());

        Transaction transaction = Transaction.builder()
                .senderWallet(wallet)
                .amount(request.getAmount())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .category(TransactionCategory.DEBIT)
                .description("Wallet withdrawal")
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(wallet);
        transactionRepository.save(transaction);
        return WalletMapper.mapToResponse(wallet);
    }
}
