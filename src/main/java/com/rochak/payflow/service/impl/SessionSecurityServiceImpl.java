package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.SessionSecurityReport;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.service.SessionSecurityService;
import com.rochak.payflow.session.SecurityProperties;
import com.rochak.payflow.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionSecurityServiceImpl implements SessionSecurityService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserSessionRepository userSessionRepository;
    private final SecurityProperties securityProperties;

    @Override
    public SessionSecurityReport generateSecurityReport() {
        long startTime = System.currentTimeMillis();

        List<String> warnings = new ArrayList<>();

        Instant now  = Instant.now();

        int usersScanned = 0;
        int sessionsScanned = 0;

        int concurrentSessionAlerts = 0;
        int expiredSessions = 0;
        int missingSessions = 0;
        int emptySessionSets = 0;
        int invalidTimestampSessions = 0;

        Set<String> userKeys = stringRedisTemplate.keys("user:*:sessions");

        if (userKeys == null || userKeys.isEmpty()) {

            return SessionSecurityReport.builder()
                    .generatedAt(now)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .usersScanned(0)
                    .sessionsScanned(0)
                    .concurrentSessionAlerts(0)
                    .expiredSessions(0)
                    .missingSessions(0)
                    .emptySessionsSets(0)
                    .invalidTimestampSessions(0)
                    .warnings(Collections.emptyList())
                    .build();
        }

        for (String userKey : userKeys) {

            usersScanned++;

            String userId = extractUserId(userKey);

            Long sessionCount = stringRedisTemplate.opsForSet().size(userKey);

            if (sessionCount == null) {
                continue;
            }

            //Concurrent Session Check

            if (hasConcurrentSessions(sessionCount)) {

                concurrentSessionAlerts++;

                warnings.add(String.format(
                        "User %s has %d active sessions.",
                        userId,
                        sessionCount
                ));
            }

            // Empty Session Set Check


            if (sessionCount == 0) {

                emptySessionSets++;

                warnings.add(String.format(
                        "[MEDIUM] Expired session detected. User %s has an empty session set.",
                        userId
                ));

                continue;
            }

            // Fetch all session ids

            Set<String> sessionIds =
                    stringRedisTemplate.opsForSet().members(userKey);

            if (sessionIds == null || sessionIds.isEmpty()) {
                continue;
            }

            // Iterate through every session

            for (String sessionId : sessionIds) {

                sessionsScanned++;

                // Missing Session Check

                Optional<UserSession> optionalSession =
                        userSessionRepository.findById(sessionId);

                if (optionalSession.isEmpty()) {

                    missingSessions++;

                    warnings.add(String.format(
                            "[HIGH] Missing session object. User=%s Session=%s",
                            userId,
                            sessionId
                    ));

                    continue;
                }

                UserSession session = optionalSession.get();

                // Expired Session Check

                if (isExpired(session)) {

                    expiredSessions++;

                    warnings.add(String.format(
                            "[HIGH] Expired session detected. User=%s Session=%s",
                            userId,
                            sessionId
                    ));
                }

                // Timestamp Validation

                if (hasInvalidTimestamp(session)) {

                    invalidTimestampSessions++;

                    warnings.add(String.format(
                            "[MEDIUM] Future login/lastUsed timestamp detected. User=%s Session=%s",
                            userId,
                            sessionId
                    ));
                }

                // Session Version Validation

                if (hasInvalidSessionVersion(session)) {

                    warnings.add(String.format(
                            "[LOW] Invalid session version (%d). User=%s Session=%s",
                            session.getSessionVersion(),
                            userId,
                            sessionId
                    ));
                }

            }

        }


        return SessionSecurityReport.builder()
                .generatedAt(now)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .usersScanned(usersScanned)
                .sessionsScanned(sessionsScanned)
                .concurrentSessionAlerts(concurrentSessionAlerts)
                .expiredSessions(expiredSessions)
                .missingSessions(missingSessions)
                .emptySessionsSets(emptySessionSets)
                .invalidTimestampSessions(invalidTimestampSessions)
                .warnings(warnings)
                .build();
    }

    private boolean hasConcurrentSessions(Long sessionCount){
        return sessionCount != null && sessionCount > securityProperties.getMaxActiveSessions();
    }

    private boolean isExpired(UserSession session){
        return session.getExpiresAt() != null && session.getExpiresAt().isBefore(Instant.now());
    }

    private boolean hasInvalidTimestamp(UserSession session){
        Instant allowedTime = Instant.now()
                .plusSeconds(securityProperties.getClockDriftToleranceSeconds());

        return (session.getLoginTime() != null && session.getLoginTime().isAfter(allowedTime)) ||
                (session.getLastUsed() != null && session.getLastUsed().isAfter(allowedTime));
    }

    private boolean hasInvalidSessionVersion(UserSession session){
        return session.getSessionVersion() <= 0;
    }

    private String extractUserId(String redisKey){
        return redisKey.replace("user:", "").replace(":sessions", "");

    }
}
