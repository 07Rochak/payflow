package com.rochak.payflow.controller.admin;

import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
@Tag(name = "Admin - Wallets", description = "Administrative wallet inspection and freeze/unfreeze operations. All endpoints require the ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminWalletController {

    public final WalletService walletService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user wallet", description = "Returns the wallet for the specified user ID. ADMIN role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet returned", content = @Content(schema = @Schema(implementation = WalletResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable("userId") Long id){
        return ResponseEntity.ok(walletService.getWalletByUserId(id));
    }

    @PostMapping("/{walletId}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Freeze wallet", description = "Freezes the specified wallet. ADMIN role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet frozen successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<String> freezeWalletByWalletId(@PathVariable("walletId") Long id)
    {
        walletService.freezeWallet(id);
        return ResponseEntity.ok("Wallet frozen successfully.");
    }

    @PostMapping("/{walletId}/unFreeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unfreeze wallet", description = "Unfreezes the specified wallet. ADMIN role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet unfrozen successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<String> unFreezeWalletByWalletId(@PathVariable("walletId") Long id)
    {
        walletService.unFreezeWallet(id);
        return ResponseEntity.ok("Wallet unfrozen successfully");
    }
}
