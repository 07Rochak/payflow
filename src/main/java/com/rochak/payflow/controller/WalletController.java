package com.rochak.payflow.controller;

import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.dto.request.WithdrawRequestDto;
import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@AllArgsConstructor
@Tag(name = "Wallet", description = "Authenticated wallet operations for balance retrieval, transfers and withdrawals.")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private WalletService walletService;

    @GetMapping("/me")
    @Operation(summary = "Get own wallet", description = "Returns the wallet belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet returned", content = @Content(schema = @Schema(implementation = WalletResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
    })
    public ResponseEntity<WalletResponseDTO> getWallet(){
        String email = SecurityUtils.getCurrentUserEmail();
        return new ResponseEntity<>(walletService.getWalletByEmail(email), HttpStatus.OK);
    }

//    @PostMapping("/{id}/add-money")
//    public ResponseEntity<WalletResponseDTO> addMoney(@PathVariable Long id,
//                                                      @RequestBody @Valid AddMoneyRequestDTO request){
//        return new ResponseEntity<>(walletService.addMoney(id, request), HttpStatus.OK);
//    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfers money from the authenticated user's wallet to another user's wallet, subject to wallet and daily transfer limits.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed", content = @Content(schema = @Schema(implementation = WalletResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, insufficient balance, frozen wallet or limit exceeded", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Target user or wallet not found", content = @Content)
    })
    public ResponseEntity<WalletResponseDTO> transferMoney(@RequestBody @Valid TransferRequestDTO requestDTO){
        String email = SecurityUtils.getCurrentUserEmail();
        return new ResponseEntity<>(walletService.transferMoney(email, requestDTO), HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws money from the authenticated user's wallet, subject to balance, wallet and daily withdrawal limits.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal completed", content = @Content(schema = @Schema(implementation = WalletResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, insufficient balance, frozen wallet or limit exceeded", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    public ResponseEntity<WalletResponseDTO> withdrawMoney(@RequestBody @Valid WithdrawRequestDto request){
        String email = SecurityUtils.getCurrentUserEmail();
        return new ResponseEntity<>(walletService.withdrawMoney(email, request), HttpStatus.OK);
    }
}
