package com.rochak.payflow.session;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@RedisHash("session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    private String sessionId;

    private Long userId;

    private String email;

    private String currentTokenId;

    private Instant loginTime;

    private Instant lastUsed;

    private String device;

    private String ip;

    @Builder.Default
    private Integer sessionVersion  = 1;

    @TimeToLive
    private Long ttl;
}
