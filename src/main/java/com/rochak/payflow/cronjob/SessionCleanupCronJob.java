package com.rochak.payflow.cronjob;

import com.rochak.payflow.service.SessionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class SessionCleanupCronJob {
    private final SessionService sessionService;

    @Scheduled(cron = "0 * * * * *")
//    @Scheduled(cron = "0 0 * * * *") // run every hour
    public void cleanupOrphanSessions() {
        log.info("Starting orphan session cleanup");
        sessionService.cleanupOrphanSessions();
        log.info("Completed orphan session cleanup.");
    }
}
