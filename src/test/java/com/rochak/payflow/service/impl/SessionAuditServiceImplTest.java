package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.SessionAuditReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionAuditServiceImplTest {
    @Mock StringRedisTemplate redisTemplate;
    @Mock SetOperations<String,String> setOperations;

    @Test void generateAuditReport_shouldCountActiveUsersAndSessions() {
        when(redisTemplate.keys("user:*:sessions")).thenReturn(Set.of("user:1:sessions","user:2:sessions"));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("user:1:sessions")).thenReturn(2L);
        when(setOperations.size("user:2:sessions")).thenReturn(1L);
        SessionAuditReport report = new SessionAuditServiceImpl(redisTemplate).generateAuditReport();
        assertEquals(2, report.getActiveUsers());
        assertEquals(3, report.getActiveSessions());
        assertEquals(2, report.getMaxSessionsPerUser());
        assertEquals(1L, report.getUserWithMostSessions());
        assertEquals(1.5, report.getAverageSessionsPerUser());
    }

    @Test void generateAuditReport_shouldReturnZerosWhenNoKeys() {
        when(redisTemplate.keys("user:*:sessions")).thenReturn(Set.of());
        SessionAuditReport report = new SessionAuditServiceImpl(redisTemplate).generateAuditReport();
        assertEquals(0, report.getActiveUsers());
        assertEquals(0, report.getActiveSessions());
    }
}
