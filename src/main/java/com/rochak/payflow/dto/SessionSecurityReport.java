package com.rochak.payflow.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
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
