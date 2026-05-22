package com.app.folioman.auth.domain;

import com.app.folioman.auth.config.JwtProperties;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public RefreshTokenEntity createRefreshToken(Long userId, String token) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiry()));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        List<RefreshTokenEntity> validUserTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(validUserTokens);
    }

    public boolean isTokenValid(RefreshTokenEntity token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now());
    }
}
