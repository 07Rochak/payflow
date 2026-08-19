package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "wallets")
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Wallet",
        description = """
        Internal persistence entity representing a PayFlow wallet.

        This schema documents the database/domain model and is not the public
        REST response contract. Public wallet APIs use WalletResponseDTO.
        """
)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique database identifier of the wallet.",
            example = "10",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Current wallet balance in INR.",
            example = "1500.0"
    )
    private Double balance;

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Schema(
            description = "User who owns this wallet. Each PayFlow user has one wallet.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private User user;

    @Column(nullable = false)
    @Schema(
            description = "Indicates whether the wallet is currently frozen by an administrator. "
                    + "Frozen wallets cannot perform applicable wallet operations.",
            example = "false"
    )
    private boolean frozen = false;
}
