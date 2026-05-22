package com.app.folioman.auth.rest;

import com.app.folioman.auth.domain.CustomUserDetailsService;
import com.app.folioman.auth.domain.JwtService;
import com.app.folioman.auth.domain.LoginAttemptService;
import com.app.folioman.auth.domain.RefreshTokenService;
import com.app.folioman.auth.domain.UserEntity;
import com.app.folioman.auth.domain.UserRepository;
import com.app.folioman.auth.rest.dto.AuthResponse;
import com.app.folioman.auth.rest.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;

    @Value("${app.jwt.access-token-expiry:1800000}")
    private long accessTokenExpiryMs;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            CustomUserDetailsService userDetailsService,
            LoginAttemptService loginAttemptService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String ipAddress = request.getRemoteAddr();

        if (loginAttemptService.isAccountLocked(username)) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, "Account is locked due to too many failed attempts.");
            problemDetail.setType(URI.create("urn:folioman:auth:locked"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            loginAttemptService.recordSuccessfulLogin(username);

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            Optional<UserEntity> userEntityOptional = userRepository.findByUsername(username);
            userEntityOptional.ifPresent(
                    userEntity -> refreshTokenService.createRefreshToken(userEntity.getId(), refreshToken));

            logger.info("Successful login for user: {} from IP: {}", username, ipAddress);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, accessTokenExpiryMs));

        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailedAttempt(username);
            logger.warn("Failed login attempt for user: {} from IP: {}", username, ipAddress);
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            problemDetail.setType(URI.create("urn:folioman:auth:invalid-credentials"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
        } catch (Exception e) {
            logger.error("Error during login for user: {}", username, e);
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        return refreshTokenService
                .findByToken(refreshToken)
                .map(token -> {
                    if (refreshTokenService.isTokenValid(token)) {
                        Optional<UserEntity> userOpt = userRepository.findById(token.getUserId());
                        if (userOpt.isPresent()) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(
                                    userOpt.get().getUsername());
                            String newAccessToken = jwtService.generateAccessToken(userDetails);

                            // Optionally rotate refresh token
                            refreshTokenService.revokeToken(refreshToken);
                            String newRefreshToken = jwtService.generateRefreshToken(userDetails);
                            refreshTokenService.createRefreshToken(userOpt.get().getId(), newRefreshToken);

                            return ResponseEntity.ok(
                                    new AuthResponse(newAccessToken, newRefreshToken, accessTokenExpiryMs));
                        }
                    }
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
                })
                .orElseGet(() -> {
                    ProblemDetail pd =
                            ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Refresh token not found");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
        return ResponseEntity.ok().build();
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
}
