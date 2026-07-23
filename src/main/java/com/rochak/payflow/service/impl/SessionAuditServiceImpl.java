package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.SessionAuditReport;
import com.rochak.payflow.service.SessionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionAuditServiceImpl implements SessionAuditService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public SessionAuditReport generateAuditReport() {

        long startTime = System.currentTimeMillis();

        int activeUsers = 0;
        int activeSessions = 0;
        int maxSessionsPerUser = 0;
        Long userWithMostSessions = null;

        Set<String> userSessionKeys = redisTemplate.keys("user:*:sessions");

        if(userSessionKeys != null){
            for(String key : userSessionKeys) {
                Long sessionCount = redisTemplate.opsForSet().size(key);

                if(sessionCount == null){
                    continue;
                }

                activeUsers++;

                activeSessions += sessionCount.intValue();

                if(sessionCount > maxSessionsPerUser){
                    maxSessionsPerUser = sessionCount.intValue();
                    String userId = key.replace("user:", "").replace(":sessions", "");

                    userWithMostSessions = Long.parseLong(userId);
                }
            }
        }

        double averageSessionPerUser = activeUsers == 0 ? 0 : (double) activeSessions / activeUsers;

        return SessionAuditReport.builder()
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .activeUsers(activeUsers)
                .activeSessions(activeSessions)
                .averageSessionsPerUser(averageSessionPerUser)
                .maxSessionsPerUser(maxSessionsPerUser)
                .userWithMostSessions(userWithMostSessions)
                .build();
    }
}
