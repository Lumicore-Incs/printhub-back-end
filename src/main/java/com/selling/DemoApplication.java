package com.selling;

import java.time.LocalDateTime;
import java.util.Optional;

import com.selling.model.User;
import com.selling.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
@RequiredArgsConstructor
public class DemoApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepo userRepo;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostConstruct
    public void initUsers() {
        try {
            Optional<User> byEmail = userRepo.findByEmail("nipuna315np@gmail.com");
            if (byEmail.isEmpty()) {
                String encodePassword = passwordEncoder.encode("1234");
                userRepo.save(new User(null, "piyumal", "nipuna315np@gmail.com", "0754585756", "ADMIN",
                        String.valueOf(LocalDateTime.now()), encodePassword));
            }
        } catch (Exception e) {
            logger.error("An error occurred during user initialization.", e);
        }
    }
}
