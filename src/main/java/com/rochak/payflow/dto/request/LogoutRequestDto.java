package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request used to invalidate a refresh-token session.")
public class LogoutRequestDto {
    @NotBlank
    @Schema(description = "Refresh token whose associated session should be invalidated.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
