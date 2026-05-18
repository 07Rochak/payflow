package com.rochak.payflow.service;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
}
