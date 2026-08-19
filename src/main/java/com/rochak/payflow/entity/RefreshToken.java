package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "RefreshToken",
        description = """
                Legacy/internal refresh-token persistence model.

                The current PayFlow authentication design stores refresh-token session state
                in Redis and performs refresh-token rotation through the Redis-backed session
                lifecycle. This entity remains in the persistence model for compatibility and
                historical reference but is not the active refresh-token storage mechanism.
                """
)public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique database identifier of the legacy refresh-token record.",
            example = "25",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    @Schema(
            description = "Refresh token credential associated with the legacy persistence model. This value is sensitive and is not returned by public API responses.",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "password"
    )
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    @Schema(
            description = "User associated with the legacy refresh-token record."
    )
    private User user;

    @Column(nullable = false)
    @Schema(
            description = "Expiration timestamp of the legacy refresh token.",
            example = "2026-08-26T12:30:00"
    )
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Schema(
            description = "Indicates whether the legacy refresh token has been revoked.",
            example = "false"
    )
    private boolean revoked;
}
