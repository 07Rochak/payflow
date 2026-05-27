package com.rochak.payflow.controller;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/user/me")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistoryByUserId()
    {
        String email = SecurityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(transactionService.getTransactionByEmailId(email));
    }
}
