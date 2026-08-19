package com.rochak.payflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@Schema(description = "Internal scheduled-job report containing Redis/session security findings. Not exposed by a REST endpoint.")
public class SessionSecurityReport {

    @Schema(
            description = "Timestamp when the security report was generated.")
    private Instant generatedAt;

    @Schema(
            description = "Execution time of the security scan in milliseconds.",
            example = "45"
    )
    private long executionTimeMs;

    @Schema(
            description = "Number of users inspected during the scan.",
            example = "10"
    )
    private int usersScanned;

    @Schema(
            description = "Number of Redis sessions inspected.",
            example = "14"
    )
    private int sessionsScanned;

    @Schema(
            description = "Number of users exceeding the configured concurrent-session threshold.",
            example = "1"
    )
    private int concurrentSessionAlerts;

    @Schema(
            description = "Number of expired sessions detected.",
            example = "2"
    )
    private int expiredSessions;

    @Schema(
            description = "Number of session references that no longer have corresponding session data.",
            example = "1"
    )
    private int missingSessions;

    @Schema(
            description = "Number of users whose Redis session index was empty.",
            example = "0"
    )
    private int emptySessionsSets;

    @Schema(
            description = "Number of sessions containing invalid timestamp information.",
            example = "0"
    )
    private int invalidTimestampSessions;

    @Schema(
            description = "Human-readable security warnings generated during the scan.",
            example = "[\"User 1 has 5 concurrent sessions\"]"
    )
    private List<String> warnings;
}
