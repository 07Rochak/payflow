package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.session.RedisKeys;
import com.rochak.payflow.session.SessionProperties;
import com.rochak.payflow.session.UserSession;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {
    private final UserSessionRepository userSessionRepository;
    private final SessionProperties sessionProperties;
    private final RedisTemplate<String, String> redisTemplate;


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

        UserSession savedSession = userSessionRepository.save(session);
        redisTemplate.opsForSet().add(RedisKeys.userSessions(user.getId()), savedSession.getSessionId());
        return savedSession;
    }

    @Override
    public Optional<UserSession> getRequiredSession(String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return userSessionRepository.findById(sessionId);
    }

    @Override
    public void deleteSession(String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        userSessionRepository.deleteById(sessionId);
        redisTemplate.opsForSet().remove(RedisKeys.userSessions(session.getUserId()), sessionId);
    }

    @Override
    public void rotateToken(String sessionId, String newTokenId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<String> getUserSessions(Long userId) {
        return redisTemplate.opsForSet().members(RedisKeys.userSessions(userId));
    }

    @Override
    public void deleteAllSessions(Long userId) {
        Set<String> sessions = getUserSessions(userId);

        if(sessions == null || sessions.isEmpty())
        {
            return;
        }

        for(String sessionId : sessions){
            userSessionRepository.deleteById(sessionId);
        }

        redisTemplate.delete(RedisKeys.userSessions(userId));
    }

    @Override
    public UserSession validateAndRotate(String sessionId, String presentedTokenId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if(!Objects.equals(session.getCurrentTokenId(), presentedTokenId)) {
            deleteSession(sessionId);
            throw new IllegalStateException("Refresh token reuse detected");
        }
        return rotateToken(session);
    }

    private UserSession rotateToken(UserSession session)
    {
        session.setCurrentTokenId(UUID.randomUUID().toString());

        session.setLastUsed(Instant.now());

        return userSessionRepository.save(session);
    }
}
