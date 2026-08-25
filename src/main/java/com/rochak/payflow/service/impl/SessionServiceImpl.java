package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.RefreshTokenReuseException;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.exception.SessionExpiredException;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.service.SessionValidationService;
import com.rochak.payflow.session.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final UserSessionRepository userSessionRepository;
    private final SessionProperties sessionProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final SessionValidationService sessionValidationService;
    private final DeviceExtractor deviceExtractor;
    private final IpExtractor ipExtractor;


    @Override
    public UserSession createSession(User user, String device, String ip) {
        UserSession session = UserSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(user.getId())
                .email(user.getEmail())
                .currentTokenId(UUID.randomUUID().toString())
                .loginTime(Instant.now())
                .lastUsed(Instant.now())
                .expiresAt(Instant.now().plusSeconds(sessionProperties.getMaxSessionLifetime()))
                .device(device)
                .ip(ip)
                .sessionVersion(1)
                .ttl(sessionProperties.getRefreshTokenTtl())
                .build();

        UserSession savedSession = userSessionRepository.save(session);
        redisTemplate.opsForSet().add(RedisKeys.userSessions(user.getId()), savedSession.getSessionId());
        return savedSession;
    }

    @Override
    public Optional<UserSession> getRequiredSession(String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return Optional.of(session);
    }

    @Override
    public void deleteSession(String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        userSessionRepository.deleteById(sessionId);
        redisTemplate.opsForSet().remove(RedisKeys.userSessions(session.getUserId()), sessionId);
    }


    @Override
    public Set<String> getUserSessions(Long userId) {
        return redisTemplate.opsForSet().members(RedisKeys.userSessions(userId));
    }

    @Override
    public void deleteAllSessions(Long userId) {
        Set<String> sessions = getUserSessions(userId);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (String sessionId : sessions) {
            userSessionRepository.deleteById(sessionId);
        }

        redisTemplate.delete(RedisKeys.userSessions(userId));
    }

    @Override
    public UserSession validateAndRotate(String sessionId, String presentedTokenId, HttpServletRequest request) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (Instant.now().isAfter(session.getExpiresAt())) {
            deleteSession(sessionId);
            throw new SessionExpiredException("Maximum session lifetime exceeded. Please log in again.");
        }

        if (!Objects.equals(session.getCurrentTokenId(), presentedTokenId)) {
            deleteSession(sessionId);
            throw new RefreshTokenReuseException("Refresh token reuse detected");
        }

        if (!session.getCurrentTokenId().equals(presentedTokenId)) {
            deleteSession(sessionId);
            throw new RefreshTokenReuseException("Refresh Token reuse detected");
        }

        String device = deviceExtractor.extract(request);
        String ip = ipExtractor.extract(request);
        log.info("Session before validation: {}", session);
        log.info("Session Version = {}", session.getSessionVersion());
        sessionValidationService.validate(session, device, ip);

        return rotateToken(session);
    }

    @Override
    public void cleanupOrphanSessions() {
        long start = System.currentTimeMillis();

        int usersScanned = 0;
        int sessionsChecked = 0;
        int orphanSessionsRemoved = 0;

        Set<String> userSessionKeys = redisTemplate.keys("user:*:sessions");

        if (userSessionKeys == null || userSessionKeys.isEmpty()) {
            log.info("No user session sets found.");
            return;
        }

        for (String userKey : userSessionKeys) {
            usersScanned++;

            try {
                Set<String> sessionIds = redisTemplate.opsForSet().members(userKey);
                if (sessionIds == null || sessionIds.isEmpty()) {
                    continue;
                }

                for (String sessionIdObj : sessionIds) {
                    sessionsChecked++;
                    String sessionId = sessionIdObj.toString();
                    boolean exists = userSessionRepository.existsById(sessionId);

                    if (!exists) {
                        redisTemplate.opsForSet().remove(userKey, sessionId);
                        orphanSessionsRemoved++;
                        log.info("Removed orphan session {} from User {}", sessionId, userKey);
                    }
                }

                Long remaining = redisTemplate.opsForSet().size(userKey);

                if (remaining != null && remaining == 0) {
                    redisTemplate.delete(userKey);
                }
            } catch (Exception e) {
                log.error("Failed processing {} : {}", userKey, e.getMessage(), e);
            }

            long duration = System.currentTimeMillis() - start;

            log.info("""
                    Session cleanup completed.
                    Users scanned: {}
                    Sessions scanned: {}
                    Orphan sessions removed: {}
                    Duration: {}ms
                    """, usersScanned, sessionsChecked, orphanSessionsRemoved, duration);
        }
    }

    private UserSession rotateToken(UserSession session) {
        session.setCurrentTokenId(UUID.randomUUID().toString());

        session.setLastUsed(Instant.now());

        session.setTtl(sessionProperties.getRefreshTokenTtl());

        return userSessionRepository.save(session);
    }
}
