package com.rochak.payflow.controller.admin;

import com.rochak.payflow.dto.response.TransactionResponseDTO;
import com.rochak.payflow.service.TransactionService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@Tag(name = "Admin - Transactions", description = "Administrative transaction inspection. All endpoints require the ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminTransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transactions for a user", description = "Returns transaction history for the specified user ID. ADMIN role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions returned", content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content)
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionByUserId(@PathVariable Long id){
        return ResponseEntity.ok(transactionService.getTransactionByUserId(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transactions", description = "Returns all transactions recorded by PayFlow. ADMIN role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions returned", content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "ADMIN role required" , content = @Content)
    })
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransaction() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
