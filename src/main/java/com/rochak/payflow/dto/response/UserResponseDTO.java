package com.rochak.payflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Public user information returned by PayFlow. Passwords and other authentication secrets are not exposed.")
public class UserResponseDTO {
    @Schema(description = "User database ID.", example = "1")
    private Long id;

    @Schema(description = "User display name.", example = "Test User")
    private String name;

    @Schema(description = "User email address.", example = "user@payflow.com")
    private String email;
}
