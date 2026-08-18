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

    private Instant generatedAt;

    private long executionTimeMs;

    private int usersScanned;

    private int sessionsScanned;

    private int concurrentSessionAlerts;

    private int expiredSessions;

    private int missingSessions;

    private int emptySessionsSets;

    private int invalidTimestampSessions;

    private List<String> warnings;
}
