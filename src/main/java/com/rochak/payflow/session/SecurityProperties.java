package com.rochak.payflow.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.session")
public class SecurityProperties {
    private int maxActiveSessions;
    private long clockDriftToleranceSeconds;
    private boolean validateDevice;
    private boolean validateIp;
}
