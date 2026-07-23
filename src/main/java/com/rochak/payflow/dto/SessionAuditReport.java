package com.rochak.payflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAuditReport {
    private LocalDateTime generatedAt;

    private Long executionTimeMs;

    private int activeUsers;

    private int activeSessions;

    private double averageSessionsPerUser;

    private int maxSessionsPerUser;

    private Long userWithMostSessions;
}
