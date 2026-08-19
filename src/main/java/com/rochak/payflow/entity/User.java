package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "User",
        description = """
        Internal persistence entity representing a PayFlow user.

        This schema documents the database/domain model and is not the public
        REST response contract. Public user APIs use UserResponseDTO.
        """
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique database identifier of the user.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Email address associated with the user account.",
            example = "user@payflow.com"
    )
    private String email;

    @Column(nullable = false, unique = true)
    @Schema(
            description = "Unique display name of the user.",
            example = "Test User"
    )
    private String name;

    @Column(nullable = false)
    @Schema(
            description = "Password credential stored internally for authentication. The actual password is never returned by public REST APIs.",
            example = "$2a$10$exampleHashedPassword",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "password"
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Authorization role assigned to the user.",
            example = "USER"
    )
    private Role role;
}
