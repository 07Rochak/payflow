package com.rochak.payflow.cronjob;

import com.rochak.payflow.dto.SessionAuditReport;
import com.rochak.payflow.service.SessionAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuditCronJob {
    private final SessionAuditService sessionAuditService;

//    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 * * * *") // run every hour
    public void auditSessions(){
        SessionAuditReport report = sessionAuditService.generateAuditReport();

        log.info(
                """
                ========== SESSION AUDIT ==========
                Generated At         : {}
                Active Users         : {}
                Active Sessions      : {}
                Avg Sessions/User    : {}
                Max Sessions/User    : {}
                User With Max        : {}
                Execution Time       : {} ms
                ===================================
                """,
                report.getGeneratedAt(),
                report.getActiveUsers(),
                report.getActiveSessions(),
                String.format("%.2f", report.getAverageSessionsPerUser()),
                report.getMaxSessionsPerUser(),
                report.getUserWithMostSessions(),
                report.getExecutionTimeMs()
        );
    }
}
