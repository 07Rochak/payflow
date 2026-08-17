package com.rochak.payflow.integration;

import com.rochak.payflow.dto.SessionSecurityReport;
import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.DeviceMismatchException;
import com.rochak.payflow.exception.IpAddressMismatchException;
import com.rochak.payflow.exception.RefreshTokenReuseException;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.security.jwt.JwtService;
import com.rochak.payflow.service.AuthService;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.service.SessionSecurityService;
import com.rochak.payflow.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
        "security.session.max-active-sessions=5",
        "security.session.clock-drift-tolerance-seconds=60",
        "security.session.validate-device=true",
        "security.session.validate-ip=true"
})
class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired UserSessionRepository userSessionRepository;
    @Autowired AuthService authService;
    @Autowired SessionService sessionService;
    @Autowired SessionSecurityService sessionSecurityService;
    @Autowired JwtService jwtService;
    @Autowired BCryptPasswordEncoder passwordEncoder;
    @Autowired RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clearRedis() {
        var keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void jwt_shouldGenerateValidateAndExtractClaims() {
        String email = "jwt-" + UUID.randomUUID() + "@test.com";

        String token = jwtService.generateToken(email, Role.USER.name());

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void refreshToken_shouldRotateTokenAndPreserveSessionIdentity() {
        User user = createUser();
        MockHttpServletRequest request = request("Chrome", "127.0.0.1");

        AuthResponseDTO login = login(user, request);
        String oldRefreshToken = login.getRefreshToken();
        String oldTokenId = jwtService.extractToken(oldRefreshToken);
        String sessionId = jwtService.extractSessionId(oldRefreshToken);

        RefreshTokenRequestDto refresh = new RefreshTokenRequestDto();
        refresh.setRefreshToken(oldRefreshToken);

        AuthResponseDTO refreshed = authService.refreshToken(refresh, request);

        String newTokenId = jwtService.extractToken(refreshed.getRefreshToken());

        assertNotEquals(oldTokenId, newTokenId);
        assertEquals(sessionId, jwtService.extractSessionId(refreshed.getRefreshToken()));
        assertNotEquals(oldRefreshToken, refreshed.getRefreshToken());

        UserSession session = userSessionRepository.findById(sessionId).orElseThrow();
        assertEquals(newTokenId, session.getCurrentTokenId());
    }

    @Test
    void refreshToken_shouldRejectReplayAndDeleteSession() {
        User user = createUser();
        MockHttpServletRequest request = request("Chrome", "127.0.0.1");

        AuthResponseDTO login = login(user, request);
        String oldRefreshToken = login.getRefreshToken();
        String sessionId = jwtService.extractSessionId(oldRefreshToken);

        RefreshTokenRequestDto firstRefresh = new RefreshTokenRequestDto();
        firstRefresh.setRefreshToken(oldRefreshToken);
        authService.refreshToken(firstRefresh, request);

        RefreshTokenRequestDto replay = new RefreshTokenRequestDto();
        replay.setRefreshToken(oldRefreshToken);

        assertThrows(
                RefreshTokenReuseException.class,
                () -> authService.refreshToken(replay, request)
        );

        assertTrue(userSessionRepository.findById(sessionId).isEmpty());
    }

    @Test
    void refreshToken_shouldRejectDeviceMismatch() {
        User user = createUser();
        MockHttpServletRequest loginRequest = request("Chrome", "127.0.0.1");
        AuthResponseDTO login = login(user, loginRequest);

        MockHttpServletRequest differentDevice = request("Firefox", "127.0.0.1");
        RefreshTokenRequestDto refresh = new RefreshTokenRequestDto();
        refresh.setRefreshToken(login.getRefreshToken());

        assertThrows(
                DeviceMismatchException.class,
                () -> authService.refreshToken(refresh, differentDevice)
        );
    }

    @Test
    void refreshToken_shouldRejectIpMismatch() {
        User user = createUser();
        MockHttpServletRequest loginRequest = request("Chrome", "127.0.0.1");
        AuthResponseDTO login = login(user, loginRequest);

        MockHttpServletRequest differentIp = request("Chrome", "10.0.0.10");
        RefreshTokenRequestDto refresh = new RefreshTokenRequestDto();
        refresh.setRefreshToken(login.getRefreshToken());

        assertThrows(
                IpAddressMismatchException.class,
                () -> authService.refreshToken(refresh, differentIp)
        );
    }

    @Test
    void sessionSecurity_shouldDetectConcurrentSessions() {
        User user = createUser();

        for (int i = 0; i < 6; i++) {
            sessionService.createSession(
                    user,
                    "Device-" + i,
                    "127.0.0." + (i + 1)
            );
        }

        SessionSecurityReport report =
                sessionSecurityService.generateSecurityReport();

        assertTrue(report.getConcurrentSessionAlerts() >= 1);
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.save(
                new User(
                        null,
                        "security-" + suffix + "@test.com",
                        "security-" + suffix,
                        passwordEncoder.encode("password123"),
                        Role.USER
                )
        );
    }

    private AuthResponseDTO login(User user, HttpServletRequest request) {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail(user.getEmail());
        login.setPassword("password123");
        return authService.login(login, request);
    }

    private MockHttpServletRequest request(String device, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", device);
        request.setRemoteAddr(ip);
        return request;
    }
}
