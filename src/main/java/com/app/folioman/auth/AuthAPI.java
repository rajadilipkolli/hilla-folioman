package com.app.folioman.auth;

import java.util.Optional;

public interface AuthAPI {
    Optional<CustomUserDetails> findUserDetailsByUsername(String username);

    Optional<CustomUserDetails> findUserDetailsById(Long id);

    void createRefreshToken(Long userId, String token);

    boolean isTokenValidAndExists(String token);

    void revokeToken(String token);

    Optional<Long> getUserIdByRefreshToken(String token);
}
