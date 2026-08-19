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
    @Schema(
            description = "Timestamp when the audit report was generated.")
    private LocalDateTime generatedAt;

    @Schema(
            description = "Execution time of the audit job in milliseconds.",
            example = "32"
    )
    private Long executionTimeMs;

    @Schema(
            description = "Number of users with active Redis sessions.",
            example = "5"
    )
    private int activeUsers;

    @Schema(
            description = "Total number of active Redis sessions.",
            example = "8"
    )
    private int activeSessions;

    @Schema(
            description = "Average number of sessions per active user.",
            example = "1.6"
    )
    private double averageSessionsPerUser;

    @Schema(
            description = "Maximum number of sessions owned by a single user.",
            example = "3"
    )
    private int maxSessionsPerUser;

    @Schema(
            description = "Database ID of the user owning the highest number of sessions.",
            example = "1"
    )
    private Long userWithMostSessions;
}
