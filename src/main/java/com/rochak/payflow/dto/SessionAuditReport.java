package com.rochak.payflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Internal scheduled-job report summarizing Redis session activity. Not exposed by a REST endpoint.")
public class SessionAuditReport {
    private LocalDateTime generatedAt;

    private Long executionTimeMs;

    private int activeUsers;

    private int activeSessions;

    private double averageSessionsPerUser;

    private int maxSessionsPerUser;

    private Long userWithMostSessions;
}
