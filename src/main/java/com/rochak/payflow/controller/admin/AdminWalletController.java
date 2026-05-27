package com.rochak.payflow.controller.admin;

import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    public final WalletService walletService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable("userId") Long id){
        System.out.println("API hit");
        return ResponseEntity.ok(walletService.getWalletByUserId(id));
    }
}
