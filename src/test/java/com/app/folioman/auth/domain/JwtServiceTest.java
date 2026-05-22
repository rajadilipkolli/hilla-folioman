package com.app.folioman.auth.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.auth.config.JwtProperties;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("testSecretKeyThatNeedsToBeLongEnoughForHmacSha256");
        jwtProperties.setAccessTokenExpiry(1800000L); // 30 mins
        jwtProperties.setRefreshTokenExpiry(172800000L); // 2 days

        jwtService = new JwtService(jwtProperties);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateAccessTokenProducesValidJwtWithExpectedClaims() {
        String token = jwtService.generateAccessToken(userDetails);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);

        Date expiration = jwtService.extractExpiration(token);
        assertTrue(expiration.after(new Date()));

        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void generateRefreshTokenProducesTokenWithLongerExpiry() {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Date accessExpiry = jwtService.extractExpiration(accessToken);
        Date refreshExpiry = jwtService.extractExpiration(refreshToken);

        assertTrue(refreshExpiry.after(accessExpiry));
    }

    @Test
    void validateTokenReturnsTrueForValidTokensAndFalseForTamperedTokens() {
        String token = jwtService.generateAccessToken(userDetails);
        assertTrue(jwtService.validateToken(token));

        String tamperedToken = token + "xyz";
        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void validateTokenReturnsFalseForExpiredTokens() {
        JwtProperties tempProps = new JwtProperties();
        tempProps.setSecret("testSecretKeyThatNeedsToBeLongEnoughForHmacSha256");
        tempProps.setAccessTokenExpiry(-1000L); // Expired 1 second ago
        JwtService tempJwtService = new JwtService(tempProps);

        String expiredToken = tempJwtService.generateAccessToken(userDetails);

        assertFalse(tempJwtService.validateToken(expiredToken));
    }

    @Test
    void extractUsernameCorrectlyParsesSubjectClaim() {
        String token = jwtService.generateAccessToken(userDetails);
        assertEquals("testuser", jwtService.extractUsername(token));
    }
}
