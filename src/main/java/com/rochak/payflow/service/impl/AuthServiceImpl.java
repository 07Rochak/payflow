package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
import com.rochak.payflow.entity.RefreshToken;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.repository.RefreshTokenRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.jwt.JwtService;
import com.rochak.payflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new RuntimeException("Invalid email or password")
                );
        if (user == null){
            throw new RuntimeException("Invalid email or password");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches){
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository
                .findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDto request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Refresh token not found")
                );

        if(refreshToken.isRevoked()){
            throw new RuntimeException("Refresh token revoked");
        }
        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(
                        refreshToken.getToken()
                )
                .build();
    }

}
