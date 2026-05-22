package com.app.folioman.auth.domain;

import com.app.folioman.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;

    JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtProperties.getAccessTokenExpiry(), ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtProperties.getRefreshTokenExpiry(), REFRESH_TOKEN_TYPE);
    }

    private String generateToken(UserDetails userDetails, long expirationMs, String tokenType) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return userDetails.getUsername().equals(username) && !isTokenExpired(token) && isAccessToken(token);
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token) && isAccessToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
