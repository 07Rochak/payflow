package com.rochak.payflow.service.impl;

import com.rochak.payflow.configs.WalletLimitConfig;
import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.request.WithdrawRequestDto;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.*;
import com.rochak.payflow.exception.InsufficientBalanceException;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.exception.WalletFrozenException;
import com.rochak.payflow.exception.WalletLimitExceededException;
import com.rochak.payflow.mapper.WalletMapper;
import com.rochak.payflow.repository.TransactionRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
@Slf4j
public class WalletServiceImpl implements WalletService {
    private WalletRepository walletRepository;
    private UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletLimitConfig walletLimitConfig;

    @Override
    public WalletResponseDTO getWalletByUserId(long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
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
        log.info("Add Money request Received. User: {}, amount: {}", userId, request.getAmount());
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        Double newBalance = wallet.getBalance() + request.getAmount();
        if(newBalance.compareTo(walletLimitConfig.getMaxBalance())>0){
            throw new WalletLimitExceededException(
                    "Maximum wallet balance exceeded. Money addition declined"
            );
        }
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        log.info("Validation Successful. Creating Transaction");
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
        log.info("Wallet Updated. Walled ID: {}, Balance: {}", wallet.getId(), wallet.getBalance());
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponseDTO transferMoney(String email, TransferRequestDTO transferRequestDTO) {
        log.info("Transfer money request received");
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

        log.info("Wallet locking done");
        Wallet sender = first.getUser().getId().equals(senderUserId) ? first : second;

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        Double todayTransferAmount = transactionRepository.getTodayTransactionAmount(
                sender.getUser().getId(),
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                startOfDay
        );

        Double projectedAmount = todayTransferAmount + transferRequestDTO.getAmount();

        if(projectedAmount > walletLimitConfig.getDailyTransferLimit()) {
            throw new WalletLimitExceededException("Daily Transfer limit exceeded");
        }

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
        if (sender.isFrozen()) {
            throw new WalletFrozenException(
                    "Wallet is frozen. Transfers are not allowed."
            );
        }

        if(user.getId().equals(transferRequestDTO.getToUserId())){
            throw new RuntimeException("Cannot transfer to same user");
        }

        if(sender.getBalance()< transferRequestDTO.getAmount()){
            throw new RuntimeException("Insufficient balance");
        }
        Double receiverNewBalance = receiver.getBalance() + transferRequestDTO.getAmount();
        if (receiverNewBalance.compareTo(walletLimitConfig.getMaxBalance()) > 0) {
            throw new WalletLimitExceededException(
                    "Maximum wallet balance exceeded"
            );
        }
        log.info("Validations complete, transaction is valid.");
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
        log.info("Transaction Completed. Sender: {}, Reciever: {}, amount: {}", sender.getId(), receiver.getId(), transferRequestDTO.getAmount());
        return WalletMapper.mapToResponse(savedSender);
    }

    @Override
    @Transactional
    public WalletResponseDTO withdrawMoney(String email, WithdrawRequestDto request) {
        log.info("Withdraw money request recived");
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User not found")
                );

        Long id = user.getId();

        Wallet wallet = walletRepository.findByUser_Id(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Wallet not found")
                );

        if (wallet.isFrozen()) {
            throw new WalletFrozenException(
                    "Wallet is frozen. Withdrawals are not allowed."
            );
        }

        if(wallet.getBalance().compareTo(request.getAmount())<0){
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        Double todayWithdrawalAmount = transactionRepository.getTodayTransactionAmount(id, TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS, startOfDay);

        double projectedWithdrawAmount = todayWithdrawalAmount + request.getAmount();

        if(projectedWithdrawAmount > walletLimitConfig.getDailyWithdrawalLimit()) {
            throw new WalletLimitExceededException("Daily withdrawal limit exceeded");
        }
        log.info("Validations successful, transaction is valid");

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
        log.info("Withdrawal completed. Wallet Id: {}, amount: {}", wallet.getId(), request.getAmount());
        return WalletMapper.mapToResponse(wallet);
    }

    @Override
    @Transactional
    public void freezeWallet(long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        if (wallet.isFrozen()) {
            throw new WalletFrozenException(
                    "Wallet is already frozen"
            );
        }
        wallet.setFrozen(true);
        walletRepository.save(wallet);
        log.info("Wallet ID: {} Frozen", walletId);
    }

    @Override
    @Transactional
    public void unFreezeWallet(long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Wallet not found")
                );
        if (!wallet.isFrozen()) {
            throw new WalletFrozenException(
                    "Wallet is already active"
            );
        }

        wallet.setFrozen(false);
        walletRepository.save(wallet);
        log.info("Wallet ID: {} unfrozen", walletId);
    }


}
