package com.rochak.payflow.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credentials used to authenticate a PayFlow user.")
public class LoginRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Schema(description = "Registered user email address.", example = "user@payflow.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password.", example = "Test@12345", format = "password")
    private String password;
}
