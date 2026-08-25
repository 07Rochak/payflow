package com.rochak.payflow;

import com.rochak.payflow.configs.WalletLimitConfig;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.session.SessionProperties;
import com.rochak.payflow.session.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties(
		WalletLimitConfig.class
)
//@Slf4j
@EnableScheduling
public class PayflowApplication {

	public static void main(String[] args) {
//		SpringApplication app = SpringApplication.run(PayflowApplication.class, args);
		SpringApplication app = new SpringApplication(PayflowApplication.class);
		app.setApplicationStartup(
				new BufferingApplicationStartup(2048)
		);

		app.run(args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}


//	@Bean
//	CommandLineRunner redisWarmup(StringRedisTemplate redisTemplate) {
//		return args -> {
//			long start = System.nanoTime();
//
//			redisTemplate.opsForValue()
//					.set("payflow:startup:warmup", "ok");
//
//			long duration =
//					(System.nanoTime() - start) / 1_000_000;
//
//			log.info("Redis warmup completed in {} ms", duration);
//		};
//	}

//	@Bean
//	CommandLineRunner run(UserRepository repo) {
//		return args -> {
//			repo.save(new User(null, "Rochak", "rochak@gmail.com", "ABCD1234"));
//		};
//	}

	// testing redis
//	@Bean
//	CommandLineRunner testRedis(UserSessionRepository userSessionRepository, SessionProperties properties)
//	{
//		return args -> {
//			UserSession session = UserSession.builder()
//					.sessionId(UUID.randomUUID().toString())
//					.userId(1L)
//					.email("test@gmail.com")
//					.currentTokenId(UUID.randomUUID().toString())
//					.loginTime(Instant.now())
//					.lastUsed(Instant.now())
//					.device("chrome")
//					.ip("127.0.0.1")
//					.ttl(properties.getRefreshTokenTtl())
//					.build();
//
//			userSessionRepository.save(session);
//
//			System.out.println(userSessionRepository.findById(session.getSessionId()));
//		};
//	}

}
