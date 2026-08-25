package com.rochak.payflow.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.security.jwt.JwtService;
import com.rochak.payflow.session.UserSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Component
public class ApplicationWarmup {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationWarmup.class);

    private final UserRepository userRepository;
    private final RedisConnectionFactory redisConnectionFactory;
    private final PasswordEncoder passwordEncoder;
    private final JsonMapper jsonMapper;
    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate; // add a plain RestTemplate bean, or reuse an existing one

    private static final String WARMUP_BCRYPT_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoO2qJ6f8QY4GJ8f6mY0J4e7Lx2";


//    public ApplicationWarmup(
//            UserRepository userRepository,
//            RedisConnectionFactory redisConnectionFactory,
//            PasswordEncoder passwordEncoder,
//            ObjectMapper objectMapper
//    ) {
//        this.userRepository = userRepository;
//        this.redisConnectionFactory = redisConnectionFactory;
//        this.passwordEncoder = passwordEncoder;
//        this.objectMapper = objectMapper;
//    }

    public ApplicationWarmup(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            RedisConnectionFactory redisConnectionFactory,
            PasswordEncoder passwordEncoder,
            JsonMapper jsonMapper,
            JwtService jwtService,
            RestTemplate restTemplate
    ) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.redisConnectionFactory = redisConnectionFactory;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {

        long totalStart = System.nanoTime();
        warmupDatabase();
        warmupRedis();
        warmupSessionRepository();
        warmupSecurityCrypto();
        warmupJackson();
        warmupHttpPipeline();
        // Only if existing JwtService API allows cleanly:
        warmupJwt();

        log.info(
                "APPLICATION WARMUP COMPLETED | total={}ms",
                elapsedMillis(totalStart)
        );
    }

    private void warmupDatabase() {
        long start = System.nanoTime();
        try {
            /*
             * Real Spring Data JPA query.
             *
             * The user does not need to exist.
             * This deliberately exercises:
             *
             * Repository proxy
             * Hibernate
             * Query generation
             * JDBC
             * PostgreSQL
             */
            userRepository.findByEmail("warmup@internal.local");
            log.info("APPLICATION WARMUP | database={}ms", elapsedMillis(start));
        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | database failed: {}", e.getMessage());
        }
    }

    private void warmupRedis() {
        long start = System.nanoTime();
        try {
            var connection = redisConnectionFactory.getConnection();
            try {
                connection.ping();
                /*
                 * Exercise actual serialization infrastructure.
                 *
                 * This is intentionally a temporary key.
                 */
                byte[] key = "payflow:warmup".getBytes();
                byte[] value = "ok".getBytes();
                connection.set(key, value);
                connection.del(key);
            } finally {
                connection.close();
            }

            log.info("APPLICATION WARMUP | redis={}ms", elapsedMillis(start));
        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | redis failed: {}", e.getMessage());
        }
    }

//    private void warmupSecurityCrypto() {
//        long start = System.nanoTime();
//        try {
//            passwordEncoder.matches(
//                    "warmup-password", WARMUP_BCRYPT_HASH
//            );
//            log.info(
//                    "APPLICATION WARMUP | securityCrypto={}ms",
//                    elapsedMillis(start)
//            );
//        } catch (Exception e) {
//            log.warn(
//                    "APPLICATION WARMUP | security crypto failed: {}",
//                    e.getMessage()
//            );
//        }
//    }
private void warmupSecurityCrypto() {
    long start = System.nanoTime();
    try {
        String realHash = passwordEncoder.encode("warmup-password");
        passwordEncoder.matches("warmup-password", realHash);
        log.info("APPLICATION WARMUP | securityCrypto={}ms", elapsedMillis(start));
    } catch (Exception e) {
        log.warn("APPLICATION WARMUP | security crypto failed: {}", e.getMessage());
    }
}

    private void warmupJackson() {
        long start = System.nanoTime();
        try {
            WarmupPayload payload = new WarmupPayload("warmup", true);
            String json = jsonMapper.writeValueAsString(payload);
            jsonMapper.readValue(json, WarmupPayload.class);

            // Also warm the actual DTOs your controllers return
            AuthResponseDTO dummyAuth = new AuthResponseDTO(/* dummy field values */);
            jsonMapper.writeValueAsString(dummyAuth);

        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | jackson failed: {}", e.getMessage());
        } finally {
            log.info("APPLICATION WARMUP | jackson={}ms", elapsedMillis(start));
        }
    }

    private void warmupSessionRepository() {
        long start = System.nanoTime();
        try {
            UserSession session = new UserSession();
            session.setSessionId("warmup-session");
            session.setUserId(0L);
            session.setEmail("warmup@internal.local");
            session.setDevice("warmup");
            session.setIp("127.0.0.1");
            session.setCurrentTokenId("warmup-token");
            session.setSessionVersion(1);
            session.setLoginTime(java.time.Instant.now());
            session.setLastUsed(java.time.Instant.now());
            /*
             * Use the same TTL value your real SessionService uses.
             * If UserSession requires expiresAt/ttl, populate those
             * exactly as your normal session creation does.
             */
            userSessionRepository.save(session);
            userSessionRepository.deleteById(session.getSessionId());
            log.info("APPLICATION WARMUP | sessionRepository={}ms", elapsedMillis(start));
        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | session repository failed: {}", e.getMessage());
        }
    }

    private void warmupJwt() {
        long start = System.nanoTime();
        try {
            // Use your existing JWT generation/validation methods.
            // Supply an in-memory/dummy User object if the existing
            // API requires one.
            String token = "...";
            jwtService.isTokenValid(token);
            log.info("APPLICATION WARMUP | jwt={}ms", elapsedMillis(start));
        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | jwt failed: {}", e.getMessage());
        }
    }

    private void warmupHttpPipeline() {
        long start = System.nanoTime();
        try {
            var body = Map.of(
                    "email", "warmup-nonexistent@payflow.internal",
                    "password", "definitely-wrong-password"
            );
            restTemplate.postForEntity(
                    "http://localhost:8080/api/auth/login",
                    body,
                    String.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // expected 401/400 — this still exercised the full pipeline
        } catch (Exception e) {
            log.warn("APPLICATION WARMUP | http pipeline failed: {}", e.getMessage());
        } finally {
            log.info("APPLICATION WARMUP | httpPipeline={}ms", elapsedMillis(start));
        }
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    private record WarmupPayload(String name, boolean active) {
    }
}