package com.app.folioman.auth.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"local", "test"})
public class UserDataSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    UserDataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            LOGGER.warn(
                    "⚠️  SECURITY WARNING: Creating default admin user with hardcoded password. CHANGE THIS IN PRODUCTION!");
            UserEntity user = new UserEntity();
            user.setUsername("admin");
            user.setEmail("admin@folioman.com");
            user.setFirstName("adminFirstName");
            user.setLastName("adminLastName");
            user.setPasswordHash(passwordEncoder.encode("admin"));
            user.setEnabled(true);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            RoleEntity userRole = roleRepository
                    .findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Required role USER not found"));
            user.getRoles().add(userRole);
            userRepository.save(user);
        }
    }
}
