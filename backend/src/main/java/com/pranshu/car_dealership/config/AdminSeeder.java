package com.pranshu.car_dealership.config;

import com.pranshu.car_dealership.auth.Role;
import com.pranshu.car_dealership.auth.User;
import com.pranshu.car_dealership.auth.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a single ADMIN account on startup so the system is usable out of the box.
 * Runs only when {@code app.seed-admin.enabled} is true (the default) and no admin
 * already exists, so restarts are idempotent and tests can opt out.
 */
@Component
@ConditionalOnProperty(name = "app.seed-admin.enabled", havingValue = "true", matchIfMissing = true)
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.seed-admin.username}") String adminUsername,
                       @Value("${app.seed-admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Seeded initial ADMIN account with username '{}'", adminUsername);
    }
}
