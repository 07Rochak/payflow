package com.rochak.payflow.cronjob;

import com.rochak.payflow.dto.SessionSecurityReport;
import com.rochak.payflow.service.SessionSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionSecurityCronJob {

    private final SessionSecurityService sessionSecurityService;

    @Scheduled(cron = "0 0 * * * *")
    public void monitorSessionSecurity() {
        SessionSecurityReport report = sessionSecurityService.generateSecurityReport();
        log.info("""
                ========== SESSION SECURITY ==========
                Generated At             : {}
                Users Scanned            : {}
                Sessions Scanned         : {}
                Concurrent Alerts        : {}
                Expired Sessions         : {}
                Missing Sessions         : {}
                Empty Session Sets       : {}
                Invalid Timestamp Count  : {}
                Execution Time           : {} ms
                =====================================
                """,
                report.getGeneratedAt(),
                report.getUsersScanned(),
                report.getSessionsScanned(),
                report.getConcurrentSessionAlerts(),
                report.getExpiredSessions(),
                report.getMissingSessions(),
                report.getEmptySessionsSets(),
                report.getInvalidTimestampSessions(),
                report.getExecutionTimeMs()
        );
        if (!report.getWarnings().isEmpty()) {

            log.warn("========== SECURITY WARNINGS ==========");

            report.getWarnings().forEach(log::warn);

            log.warn("=======================================");
        }
    }
}
