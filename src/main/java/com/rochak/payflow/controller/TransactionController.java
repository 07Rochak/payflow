package com.rochak.payflow.controller;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transactions", description = "Transaction history for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/user/me")
    @Operation(summary = "Get own transaction history", description = "Returns transactions associated with the authenticated user. The user identity is resolved from the JWT security context.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistoryByUserId()
    {
        String email = SecurityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(transactionService.getTransactionByEmailId(email));
    }
}
