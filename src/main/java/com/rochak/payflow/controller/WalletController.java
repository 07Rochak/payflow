package com.rochak.payflow.controller;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.WalletService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@AllArgsConstructor
public class WalletController {
    private WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponseDTO> getWallet(){
        String email = SecurityUtils.getCurrentUserEmail();
        return new ResponseEntity<>(walletService.getWalletByEmail(email), HttpStatus.OK);
    }

    @PostMapping("/{id}/add-money")
    public ResponseEntity<WalletResponseDTO> addMoney(@PathVariable Long id,
                                                      @RequestBody @Valid AddMoneyRequestDTO request){
        return new ResponseEntity<>(walletService.addMoney(id, request), HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<WalletResponseDTO> transferMoney(@RequestBody @Valid TransferRequestDTO requestDTO){
        return new ResponseEntity<>(walletService.transferMoney(requestDTO), HttpStatus.OK);
    }
}
