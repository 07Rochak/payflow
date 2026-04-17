package com.rochak.payflow;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PayflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayflowApplication.class, args);
	}

//	@Bean
//	CommandLineRunner run(UserRepository repo) {
//		return args -> {
//			repo.save(new User(null, "Rochak", "rochak@gmail.com", "ABCD1234"));
//		};
//	}

}
