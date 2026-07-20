package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.session.SessionProperties;
import com.rochak.payflow.session.UserSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private final UserSessionRepository userSessionRepository;
    private final SessionProperties sessionProperties;


    @Override
    public UserSession createSession(User user, String device, String ip) {
        UserSession session = UserSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(user.getId())
                .email(user.getEmail())
                .currentTokenId(UUID.randomUUID().toString())
                .loginTime(Instant.now())
                .lastUsed(Instant.now())
                .device(device)
                .ip(ip)
                .sessionVersion(1)
                .ttl(sessionProperties.getRefreshTokenTtl())
                .build();

        return userSessionRepository.save(session);
    }

    @Override
    public Optional<UserSession> findBySessionId(String sessionId) {
        return userSessionRepository.findById(sessionId);
    }

    @Override
    public void deleteSession(String sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    @Override
    public void updateSession(String sessionId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void rotateToken(String sessionId, String newTokenId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
