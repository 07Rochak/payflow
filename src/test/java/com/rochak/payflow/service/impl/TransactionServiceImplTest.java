package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.repository.TransactionRepository;
import com.rochak.payflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TransactionServiceImpl service;

    @Test void getTransactionByEmailId_shouldReturnTransactions() {
        User user = new User(1L, "a@b.com", "A", "p", com.rochak.payflow.entity.Role.USER);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findBySenderWallet_User_IdOrReceiverWallet_User_Id(1L,1L)).thenReturn(List.of());
        assertNotNull(service.getTransactionByEmailId("a@b.com"));
        verify(transactionRepository).findBySenderWallet_User_IdOrReceiverWallet_User_Id(1L,1L);
    }

    @Test void getTransactionByEmailId_shouldThrowWhenUserMissing() {
        when(userRepository.findByEmail("missing")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.getTransactionByEmailId("missing"));
    }

    @Test void getTransactionByUserId_shouldThrowWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.getTransactionByUserId(1L));
    }

    @Test void getAllTransactions_shouldReturnMappedList() {
        when(transactionRepository.findAll()).thenReturn(List.of());
        assertTrue(service.getAllTransactions().isEmpty());
    }
}
