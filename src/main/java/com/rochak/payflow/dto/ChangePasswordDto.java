package com.rochak.payflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request used to change the authenticated user's password.")
public class ChangePasswordDto {
    @NotBlank
    @Schema(description = "Current password used for verification.", example = "Test@12345", format = "password")
    private String oldPassword;

    @NotBlank
    @Schema(description = "New password.", example = "Test@54321", format = "password")
    private String newPassword;
}
