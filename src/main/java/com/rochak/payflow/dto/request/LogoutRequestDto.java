package com.rochak.payflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestDto {
    @NotBlank
    private String refreshToken;
}
