package com.rochak.payflow.service.impl;

import com.rochak.payflow.configs.WalletLimitConfig;
import com.rochak.payflow.dto.request.*;
import com.rochak.payflow.entity.*;
import com.rochak.payflow.exception.*;
import com.rochak.payflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {
    @Mock WalletRepository walletRepository;
    @Mock UserRepository userRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock WalletLimitConfig limits;
    private WalletServiceImpl service;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new WalletServiceImpl(walletRepository, userRepository, transactionRepository, limits);
        user = new User(1L, "a@b.com", "A", "p", Role.USER);
        wallet = new Wallet(10L, 100.0, user, false);
    }

    @Test void getWalletByUserId_shouldReturnWallet() {
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        assertEquals(100.0, service.getWalletByUserId(1L).getBalance());
    }

    @Test void getWalletByUserId_shouldThrowWhenMissing() {
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getWalletByUserId(1L));
    }

    @Test void creditWallet_shouldCreditAndCreateTransaction() {
        when(limits.getMaxBalance()).thenReturn(1000.0);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);
        var response = service.creditWallet(1L,50.0,"Topup","pay_1");
        assertEquals(150.0,response.getBalance());
        verify(transactionRepository).save(argThat(t -> t.getAmount()==50.0 && t.getTransactionType()==TransactionType.DEPOSIT));
        verify(walletRepository).save(wallet);
    }

    @Test void creditWallet_shouldRejectMaximumBalance() {
        when(limits.getMaxBalance()).thenReturn(120.0);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        when(limits.getMaxBalance()).thenReturn(120.0);
        assertThrows(WalletLimitExceededException.class, () -> service.creditWallet(1L,50.0,"Topup","pay_1"));
        verify(transactionRepository,never()).save(any());
    }

    @Test void withdrawMoney_shouldRejectFrozenWallet() {
        wallet.setFrozen(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        WithdrawRequestDto request = new WithdrawRequestDto(); request.setAmount(10.0);
        assertThrows(WalletFrozenException.class, () -> service.withdrawMoney("a@b.com",request));
    }

    @Test void withdrawMoney_shouldRejectInsufficientBalance() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        WithdrawRequestDto request = new WithdrawRequestDto(); request.setAmount(150.0);
        assertThrows(InsufficientBalanceException.class, () -> service.withdrawMoney("a@b.com",request));
    }

    @Test void transferMoney_shouldTransferBetweenWallets() {
        when(limits.getMaxBalance()).thenReturn(1000.0);
        when(limits.getDailyTransferLimit()).thenReturn(500.0);
        User receiverUser = new User(2L,"b@b.com","B","p",Role.USER);
        Wallet receiver = new Wallet(20L,50.0,receiverUser,false);
        TransferRequestDTO request = new TransferRequestDTO(); request.setToUserId(2L); request.setAmount(25.0);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(walletRepository.findUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findUserIdForUpdate(2L)).thenReturn(Optional.of(receiver));
        when(transactionRepository.getTodayTransactionAmount(anyLong(),eq(TransactionType.TRANSFER),eq(TransactionStatus.SUCCESS),any())).thenReturn(0.0);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        var response=service.transferMoney("a@b.com",request);
        assertEquals(75.0,wallet.getBalance());
        assertEquals(75.0,receiver.getBalance());
        assertEquals(75.0,response.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }
}
