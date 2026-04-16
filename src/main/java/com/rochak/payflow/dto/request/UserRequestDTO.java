package com.rochak.payflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message="name is required")
    private String name;

    @Email(message = "Invalid email address")
    private String email;

}