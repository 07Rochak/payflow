package com.rochak.payflow.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payflow.session")
@Getter
@Setter
public class SessionProperties {
    private Long refreshTokenTtl;
    private Long maxSessionLifetime;
}
