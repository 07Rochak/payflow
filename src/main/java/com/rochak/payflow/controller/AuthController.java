package com.rochak.payflow.controller;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.LogoutRequestDto;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
import com.rochak.payflow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request, HttpServletRequest httpServletRequest)
    {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid RefreshTokenRequestDto token, HttpServletRequest request){
        return ResponseEntity.ok(authService.refreshToken(token, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody @Valid LogoutRequestDto request){
        authService.logout(request);

        return ResponseEntity.ok("Logged out successfully");
    }
}
