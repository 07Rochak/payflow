package com.rochak.payflow.service;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.LogoutRequestDto;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest);
    AuthResponseDTO refreshToken(RefreshTokenRequestDto token, HttpServletRequest request);
    void logout(LogoutRequestDto request);
}
