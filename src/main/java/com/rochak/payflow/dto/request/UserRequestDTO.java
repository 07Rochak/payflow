package com.rochak.payflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fields that an authenticated user can modify on their own profile.")
public class UserRequestDTO {
    @NotBlank(message="name is required")
    @Schema(description = "Updated display name.", example = "Updated User")
    private String name;

    @Email(message = "Invalid email address")
    @Schema(description = "Updated email address. Changing email invalidates all existing sessions and requires login again.", example = "newuser@payflow.com")
    private String email;

}