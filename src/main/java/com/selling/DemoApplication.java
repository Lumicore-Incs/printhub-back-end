package com.selling;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.selling.model.RoleEnum;
import com.selling.model.StatusEnum;
import com.selling.model.User;
import com.selling.repository.UserRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
@RequiredArgsConstructor
public class DemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@PostConstruct
	public void initUsers() {
		try {
			Optional<User> byEmail = userRepo.findByEmail("admin@printhub.com");
			if (byEmail.isEmpty()) {
				User admin = User.builder()
						.name("Admin User")
						.email("admin@printhub.com")
						.password(passwordEncoder.encode("admin123"))
						.role(RoleEnum.SUPERUSER)
						.status(StatusEnum.ACTIVE)
						.mobile("0754585756")
						.build();
				userRepo.save(admin);
				logger.info("Default admin user initialized.");
			}
		} catch (Exception e) {
			logger.error("An error occurred during user initialization.", e);
		}
	}
}
