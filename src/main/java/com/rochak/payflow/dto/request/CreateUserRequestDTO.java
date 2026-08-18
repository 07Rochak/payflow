package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration details. The role is assigned by the endpoint and is not client-controlled.")
public class CreateUserRequestDTO {
    @NotBlank(message="name is required")
    @Schema(description = "User's display name.", example = "Test User")
    private String name;

    @Email(message = "Invalid email address")
    @Schema(description = "Unique email address used for authentication.", example = "user@payflow.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min=6, message = "Password must be of atleast 6 characters")
    @Schema(description = "Account password. Minimum 6 characters.", example = "Test@12345", format = "password", minLength = 6)
    private String password;

}