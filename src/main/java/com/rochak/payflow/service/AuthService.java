package com.rochak.payflow.service;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.LogoutRequestDto;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO refreshToken(RefreshTokenRequestDto token);
    void logout(LogoutRequestDto request);
}
