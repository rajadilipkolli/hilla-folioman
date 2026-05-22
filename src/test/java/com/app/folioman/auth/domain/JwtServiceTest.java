package com.app.folioman.auth.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the properties that would normally be injected by @Value
        ReflectionTestUtils.setField(jwtService, "secretKey", "testSecretKeyThatNeedsToBeLongEnoughForHmacSha256");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 1800000L); // 30 mins
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiryMs", 172800000L); // 2 days

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
        // Temporarily set a very short expiry for testing expiration
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", -1000L); // Expired 1 second ago

        String expiredToken = jwtService.generateAccessToken(userDetails);

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void extractUsernameCorrectlyParsesSubjectClaim() {
        String token = jwtService.generateAccessToken(userDetails);
        assertEquals("testuser", jwtService.extractUsername(token));
    }
}
