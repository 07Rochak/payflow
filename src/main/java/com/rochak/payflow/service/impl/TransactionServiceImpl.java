package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.mapper.TransactionMapper;
import com.rochak.payflow.repository.TransactionRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public List<TransactionResponseDTO> getTransactionByUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        Long userId = user.getId();
        List<Transaction> transactions = transactionRepository.findBySenderWallet_User_IdOrReceiverWallet_User_Id(
                userId, userId
        );
        return transactions.stream()
                .map(TransactionMapper::mapToResponse)
                .collect(Collectors.toList());
    }
}
