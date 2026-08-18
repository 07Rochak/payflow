package com.rochak.payflow.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Authentication tokens returned after successful login or refresh.")
public class AuthResponseDTO {
    @Schema(description = "Short-lived JWT access token used in the Authorization header.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "Stateful refresh token associated with the Redis session.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
