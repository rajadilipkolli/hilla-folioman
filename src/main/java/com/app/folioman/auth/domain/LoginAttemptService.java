package com.app.folioman.auth.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_DURATION_MINUTES = 15;

    LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordFailedAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockExpiresAt(Instant.now().plus(LOCK_TIME_DURATION_MINUTES, ChronoUnit.MINUTES));
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockExpiresAt(null);
            userRepository.save(user);
        });
    }

    @Transactional
    public boolean isAccountLocked(String username) {
        return userRepository
                .findByUsername(username)
                .map(user -> {
                    if (user.isAccountLocked()) {
                        if (user.getLockExpiresAt() != null
                                && user.getLockExpiresAt().isBefore(Instant.now())) {
                            // Lock has expired, unlock account
                            user.setAccountLocked(false);
                            user.setLockExpiresAt(null);
                            user.setFailedLoginAttempts(0);
                            userRepository.save(user);
                            return false;
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
