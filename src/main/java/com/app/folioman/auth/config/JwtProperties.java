package com.app.folioman.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret key cannot be blank")
    @Size(min = 32, message = "JWT secret key must be at least 32 characters")
    private @Nullable String secret;

    @Positive(message = "Access token expiry must be positive")
    private Long accessTokenExpiry = 1800000L;

    @Positive(message = "Refresh token expiry must be positive")
    private Long refreshTokenExpiry = 172800000L;

    private boolean secureCookies = true;

    public String getSecret() {
        return Objects.requireNonNull(secret, "JWT secret key must be configured");
    }

    public void setSecret(@Nullable String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(long accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(long refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public boolean isSecureCookies() {
        return secureCookies;
    }

    public void setSecureCookies(boolean secureCookies) {
        this.secureCookies = secureCookies;
    }
}
