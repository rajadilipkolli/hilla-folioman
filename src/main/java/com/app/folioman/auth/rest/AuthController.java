package com.app.folioman.auth.rest;

import com.app.folioman.auth.AuthAPI;
import com.app.folioman.auth.CustomUserDetails;
import com.app.folioman.auth.config.JwtProperties;
import com.app.folioman.auth.domain.CustomUserDetailsService;
import com.app.folioman.auth.domain.JwtService;
import com.app.folioman.auth.domain.LoginAttemptService;
import com.app.folioman.auth.domain.TokenBlacklistService;
import com.app.folioman.auth.rest.dto.AuthResponse;
import com.app.folioman.auth.rest.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final AuthAPI authAPI;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            LoginAttemptService loginAttemptService,
            AuthAPI authAPI,
            JwtProperties jwtProperties,
            TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.loginAttemptService = loginAttemptService;
        this.authAPI = authAPI;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String username = loginRequest.getUsername();
        String ipAddress = getClientIpAddress(request);

        if (loginAttemptService.isAccountLocked(username)) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, "Account is locked due to too many failed attempts.");
            problemDetail.setType(URI.create("urn:folioman:auth:locked"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                throw new IllegalStateException("Authentication principal was not a UserDetails instance");
            }

            loginAttemptService.recordSuccessfulLogin(username);

            Optional<CustomUserDetails> userEntityOptional =
                    authAPI.findUserDetailsByUsername(userDetails.getUsername());
            String email = userEntityOptional.map(CustomUserDetails::getEmail).orElse(userDetails.getUsername());

            String accessToken = jwtService.generateAccessToken(userDetails, email);
            String refreshToken = jwtService.generateRefreshToken(userDetails, email);

            userEntityOptional.ifPresent(userEntity -> authAPI.createRefreshToken(userEntity.getId(), refreshToken));

            LOGGER.info("Successful login for user: {} from IP: {}", maskUsername(username), maskIp(ipAddress));

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(request.isSecure() || jwtProperties.isSecureCookies())
                    .path("/api/auth")
                    .maxAge(jwtProperties.getRefreshTokenExpiry() / 1000)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(accessToken, jwtProperties.getAccessTokenExpiry()));

        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailedAttempt(username);
            LOGGER.warn("Failed login attempt for user: {} from IP: {}", maskUsername(username), maskIp(ipAddress));
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            problemDetail.setType(URI.create("urn:folioman:auth:invalid-credentials"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
        } catch (Exception e) {
            LOGGER.error("Error during login for user: {}", maskUsername(username), e);
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
        }
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refreshToken(
            @CookieValue(required = false) @Nullable String refreshToken, HttpServletRequest request) {
        if (refreshToken == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Refresh token is required");
            pd.setType(URI.create("urn:folioman:auth:missing-refresh-token"));
            return ResponseEntity.badRequest()
                    .header(
                            HttpHeaders.SET_COOKIE,
                            expiredRefreshCookie(request).toString())
                    .body(pd);
        }

        if (!authAPI.isTokenValidAndExists(refreshToken)) {
            return unauthorizedResponse();
        }

        Optional<CustomUserDetails> userOpt =
                authAPI.getUserIdByRefreshToken(refreshToken).flatMap(authAPI::findUserDetailsById);

        if (userOpt.isEmpty()) {
            return unauthorizedResponse();
        }

        CustomUserDetails user = userOpt.get();
        if (!user.isEnabled() || loginAttemptService.isAccountLocked(user.getUsername())) {
            authAPI.revokeToken(refreshToken);
            ProblemDetail pd =
                    ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "User account is locked or disabled");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String email = user.getEmail();
        String newAccessToken = jwtService.generateAccessToken(userDetails, email);

        // Rotate refresh token
        authAPI.revokeToken(refreshToken);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails, email);
        authAPI.createRefreshToken(user.getId(), newRefreshToken);

        ResponseCookie newCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(request.isSecure() || jwtProperties.isSecureCookies())
                .path("/api/auth")
                .maxAge(jwtProperties.getRefreshTokenExpiry() / 1000)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(new AuthResponse(newAccessToken, jwtProperties.getAccessTokenExpiry()));
    }

    private ResponseEntity<ProblemDetail> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));
    }

    private ResponseCookie expiredRefreshCookie(HttpServletRequest request) {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(request.isSecure() || jwtProperties.isSecureCookies())
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) @Nullable String cookieToken,
            @Nullable @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletRequest request) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String jti = jwtService.extractJti(token);
                Date expiration = jwtService.extractExpiration(token);
                if (expiration != null) {
                    long remainingTtl = expiration.getTime() - System.currentTimeMillis();
                    if (remainingTtl > 0) {
                        tokenBlacklistService.blacklist(jti, remainingTtl);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to extract or blacklist token during logout", e);
            }
        }

        if (cookieToken != null) {
            authAPI.revokeToken(cookieToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(request.isSecure() || jwtProperties.isSecureCookies())
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                return ResponseEntity.ok(Map.of("username", username, "valid", true));
            }
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    private String maskUsername(String username) {
        if (username == null || username.length() < 3) return "***";
        return username.substring(0, 2) + "***";
    }

    private String maskIp(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        if (ip.contains(":")) {
            return "IPv6_masked";
        }
        return "***";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
