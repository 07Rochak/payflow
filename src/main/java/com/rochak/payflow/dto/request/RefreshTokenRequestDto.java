package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Refresh token used to obtain a new access/refresh token pair.")
public class RefreshTokenRequestDto {
    @NotBlank
    @Schema(description = "Refresh token returned by login or a previous refresh operation.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
