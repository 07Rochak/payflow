package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.LogoutRequestDto;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
//import com.rochak.payflow.entity.RefreshToken;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.ResourceNotFoundException;
//import com.rochak.payflow.repository.RefreshTokenRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.jwt.JwtService;
import com.rochak.payflow.service.AuthService;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.session.DeviceExtractor;
import com.rochak.payflow.session.IpExtractor;
import com.rochak.payflow.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final DeviceExtractor deviceExtractor;
    private final IpExtractor ipExtractor;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest) {
        log.info("Login request recieved for email: {}", request.getEmail());
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

        String device = deviceExtractor.extract(httpServletRequest);

        String ip = ipExtractor.extract(httpServletRequest);

        UserSession session = sessionService.createSession(user, device, ip);

        log.info("Session created for user. UserId = {}, TokenId={}", user.getId(), session);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        log.info("Token generated successfully. UserId = {}, TokenId = {}", user.getId(), token);

        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), session.getSessionId(), session.getCurrentTokenId());

        log.info("Refresh Token generated successfully. UserId = {}, TokenId = {}", user.getId(), refreshToken);
//        RefreshToken refreshTokenEntity = RefreshToken.builder()
//                .token(refreshToken)
//                .user(user)
//                .expiryDate(LocalDateTime.now().plusDays(7))
//                .revoked(false)
//                .build();
//
//        refreshTokenRepository
//                .findByUser(user)
//                .ifPresent(refreshTokenRepository::delete);
//
//        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDto request, HttpServletRequest httpRequest) {

        String refreshToken = request.getRefreshToken();

        // validate signature and expiry
        if(!jwtService.isTokenValid(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }

        // extract values from jwt
        String email = jwtService.extractEmail(refreshToken);
        String sessionId = jwtService.extractSessionId(refreshToken);
        String tokenId = jwtService.extractToken(refreshToken);

        log.info("Extracted values before Refresh: Email: {}, Session ID: {}, Token ID: {}", email, sessionId, tokenId);

        // validate session and rotate refresh token
        UserSession session = sessionService.validateAndRotate(sessionId, tokenId, httpRequest);

        // Load user
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        // Generate new tokens
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), session.getSessionId(), session.getCurrentTokenId());

        log.info("New Access Tokens for UserId: {}, Access Token: {}, Refresh Token: {}", user.getId(), accessToken, newRefreshToken);
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
//        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
//                .orElseThrow(
//                        () -> new ResourceNotFoundException("Refresh token not found")
//                );
//
//        if(refreshToken.isRevoked()){
//            throw new RuntimeException("Refresh token revoked");
//        }
//        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
//            throw new RuntimeException("Refresh token expired");
//        }
//
//        User user = refreshToken.getUser();
//
//        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
//
//        return AuthResponseDTO.builder()
//                .accessToken(accessToken)
//                .refreshToken(
//                        refreshToken.getToken()
//                )
//                .build();
    }

    @Override
    public void logout(LogoutRequestDto request) {
//        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
//                .orElseThrow(
//                        () -> new ResourceNotFoundException("Refresh token not found")
//                );
//
//        refreshToken.setRevoked(true);
//        refreshTokenRepository.save(refreshToken);
        String refreshToken = request.getRefreshToken();
        if(!jwtService.isTokenValid(refreshToken)){
            return;
        }

        String sessionId = jwtService.extractSessionId(refreshToken);
        log.info("Logging out user. Email: {}, Session ID: {}, Refresh Token: {}", jwtService.extractEmail(refreshToken), sessionId, refreshToken);
        sessionService.deleteSession(sessionId);
    }

}
