package com.rochak.payflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Wallet information for a PayFlow user.")
public class WalletResponseDTO {
    @Schema(description = "Wallet database ID.", example = "10")
    private long walletId;

    @Schema(description = "Associated user database ID.", example = "1")
    private long userId;

    @Schema(description = "Current wallet balance in INR.", example = "1500.0")
    private double balance;
}
