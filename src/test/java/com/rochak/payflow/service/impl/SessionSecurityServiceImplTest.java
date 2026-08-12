package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.SessionSecurityReport;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.session.SecurityProperties;
import com.rochak.payflow.session.UserSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionSecurityServiceImplTest {
    @Mock StringRedisTemplate redisTemplate;
    @Mock UserSessionRepository repository;
    @Mock SetOperations<String,String> setOperations;

    private SecurityProperties properties() {
        SecurityProperties p = new SecurityProperties();
        p.setMaxActiveSessions(2);
        p.setClockDriftToleranceSeconds(60);
        return p;
    }

    @Test void generateSecurityReport_shouldDetectMissingSession() {
        when(redisTemplate.keys("user:*:sessions")).thenReturn(Set.of("user:1:sessions"));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("user:1:sessions")).thenReturn(1L);
        when(setOperations.members("user:1:sessions")).thenReturn(Set.of("s1"));
        when(repository.findById("s1")).thenReturn(Optional.empty());

        SessionSecurityReport report = new SessionSecurityServiceImpl(redisTemplate, repository, properties()).generateSecurityReport();
        assertEquals(1, report.getUsersScanned());
        assertEquals(1, report.getSessionsScanned());
        assertEquals(1, report.getMissingSessions());
        assertFalse(report.getWarnings().isEmpty());
    }

    @Test void generateSecurityReport_shouldDetectConcurrentSessions() {
        when(redisTemplate.keys("user:*:sessions")).thenReturn(Set.of("user:1:sessions"));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("user:1:sessions")).thenReturn(3L);
        when(setOperations.members("user:1:sessions")).thenReturn(Set.of());

        SessionSecurityReport report = new SessionSecurityServiceImpl(redisTemplate, repository, properties()).generateSecurityReport();
        assertEquals(1, report.getConcurrentSessionAlerts());
    }
}
