package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Role",
        description = "Authorization role assigned to a PayFlow user."
)
public enum Role {
    @Schema(description = "Standard PayFlow user with access to personal wallet and payment operations.")
    USER,
    @Schema(description = "Administrator with access to administrative user, wallet and transaction operations.")
    ADMIN
}
