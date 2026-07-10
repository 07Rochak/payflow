package com.rochak.payflow.controller.admin;

import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    public final WalletService walletService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable("userId") Long id){
        return ResponseEntity.ok(walletService.getWalletByUserId(id));
    }

    @PostMapping("/{walletId}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> freezeWalletByWalletId(@PathVariable("walletId") Long id)
    {
        walletService.freezeWallet(id);
        return ResponseEntity.ok("Wallet frozen successfully.");
    }

    @PostMapping("/{walletId}/unFreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unFreezeWalletByWalletId(@PathVariable("walletId") Long id)
    {
        walletService.unFreezeWallet(id);
        return ResponseEntity.ok("Wallet unfrozen successfully");
    }
}
