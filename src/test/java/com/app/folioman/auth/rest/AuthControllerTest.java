package com.app.folioman.auth.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.auth.config.JwtProperties;
import com.app.folioman.auth.domain.CustomUserDetailsService;
import com.app.folioman.auth.domain.JwtService;
import com.app.folioman.auth.domain.LoginAttemptService;
import com.app.folioman.auth.domain.RefreshTokenEntity;
import com.app.folioman.auth.domain.RefreshTokenService;
import com.app.folioman.auth.domain.TokenBlacklistService;
import com.app.folioman.auth.domain.UserEntity;
import com.app.folioman.auth.domain.UserManagementService;
import com.app.folioman.auth.rest.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Execution(ExecutionMode.SAME_THREAD)
class AuthControllerTest {

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private UserManagementService userManagementService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void login_whenAccountLocked_returns401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(true);

        var result = mockMvcTester
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .isEqualTo("Account is locked due to too many failed attempts.");
    }

    @Test
    void login_whenBadCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        var result = mockMvcTester
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .isEqualTo("Invalid credentials");

        verify(loginAttemptService).recordFailedAttempt("testuser");
    }

    @Test
    void login_whenException_returns500() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("unexpected"));

        var result = mockMvcTester
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void login_success_returnsTokens() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        UserDetails userDetails =
                User.withUsername("testuser").password("pass").roles("USER").build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtService.generateAccessToken(eq(userDetails), any())).thenReturn("access-token-123");
        when(jwtService.generateRefreshToken(eq(userDetails), any())).thenReturn("refresh-token-123");

        UserEntity ue = new UserEntity();
        ue.setId(1L);
        ue.setUsername("testuser");
        when(userManagementService.findByUsername("testuser")).thenReturn(Optional.of(ue));
        when(jwtProperties.getRefreshTokenExpiry()).thenReturn(3600000L);
        when(jwtProperties.getAccessTokenExpiry()).thenReturn(1800000L);

        var result = mockMvcTester
                .post()
                .uri("/api/auth/login")
                .header("X-Forwarded-For", "127.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.accessToken")
                .asString()
                .isEqualTo("access-token-123");

        assertThat(result.getResponse().getCookie("refreshToken")).isNotNull();
        assertThat(result.getResponse().getCookie("refreshToken").getValue()).isEqualTo("refresh-token-123");
        assertThat(result.getResponse().getCookie("refreshToken").isHttpOnly()).isTrue();

        verify(loginAttemptService).recordSuccessfulLogin("testuser");
        verify(refreshTokenService).createRefreshToken(1L, "refresh-token-123");
    }

    @Test
    void refresh_missingCookie_returns400() throws Exception {
        var result = mockMvcTester.post().uri("/api/auth/refresh").exchange();

        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .isEqualTo("Refresh token is required");
    }

    @Test
    void refresh_tokenNotFound_returns401() throws Exception {
        when(refreshTokenService.findByToken("token")).thenReturn(Optional.empty());

        var result = mockMvcTester
                .post()
                .uri("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "token"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_tokenInvalid_returns401() throws Exception {
        RefreshTokenEntity rfe = new RefreshTokenEntity();
        when(refreshTokenService.findByToken("token")).thenReturn(Optional.of(rfe));
        when(refreshTokenService.isTokenValid(rfe)).thenReturn(false);

        var result = mockMvcTester
                .post()
                .uri("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "token"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_userNotFound_returns401() throws Exception {
        RefreshTokenEntity rfe = new RefreshTokenEntity();
        rfe.setUserId(1L);
        when(refreshTokenService.findByToken("token")).thenReturn(Optional.of(rfe));
        when(refreshTokenService.isTokenValid(rfe)).thenReturn(true);
        when(userManagementService.findById(1L)).thenReturn(Optional.empty());

        var result = mockMvcTester
                .post()
                .uri("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "token"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_userLocked_returns403() throws Exception {
        RefreshTokenEntity rfe = new RefreshTokenEntity();
        rfe.setUserId(1L);
        when(refreshTokenService.findByToken("token")).thenReturn(Optional.of(rfe));
        when(refreshTokenService.isTokenValid(rfe)).thenReturn(true);

        UserEntity ue = new UserEntity();
        ue.setUsername("user");
        ue.setEnabled(true);
        when(userManagementService.findById(1L)).thenReturn(Optional.of(ue));
        when(loginAttemptService.isAccountLocked("user")).thenReturn(true);

        var result = mockMvcTester
                .post()
                .uri("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "token"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);

        verify(refreshTokenService).revokeToken("token");
    }

    @Test
    void refresh_success_returnsNewTokens() throws Exception {
        RefreshTokenEntity rfe = new RefreshTokenEntity();
        rfe.setUserId(1L);
        when(refreshTokenService.findByToken("token")).thenReturn(Optional.of(rfe));
        when(refreshTokenService.isTokenValid(rfe)).thenReturn(true);

        UserEntity ue = new UserEntity();
        ue.setId(1L);
        ue.setUsername("user");
        ue.setEnabled(true);
        when(userManagementService.findById(1L)).thenReturn(Optional.of(ue));
        when(loginAttemptService.isAccountLocked("user")).thenReturn(false);

        UserDetails userDetails =
                User.withUsername("user").password("pass").roles("USER").build();
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);

        when(jwtService.generateAccessToken(eq(userDetails), any())).thenReturn("new-access");
        when(jwtService.generateRefreshToken(eq(userDetails), any())).thenReturn("new-refresh");
        when(jwtProperties.getRefreshTokenExpiry()).thenReturn(3600000L);
        when(jwtProperties.getAccessTokenExpiry()).thenReturn(1800000L);

        var result = mockMvcTester
                .post()
                .uri("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "token"))
                .exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.accessToken")
                .asString()
                .isEqualTo("new-access");

        assertThat(result.getResponse().getCookie("refreshToken").getValue()).isEqualTo("new-refresh");

        verify(refreshTokenService).revokeToken("token");
        verify(refreshTokenService).createRefreshToken(1L, "new-refresh");
    }

    @Test
    void logout_withTokens_revokesAndBlacklists() throws Exception {
        when(jwtService.extractJti("access")).thenReturn("jti-123");
        when(jwtService.extractExpiration("access")).thenReturn(new Date(System.currentTimeMillis() + 10000));

        var result = mockMvcTester
                .post()
                .uri("/api/auth/logout")
                .header("Authorization", "Bearer access")
                .cookie(new Cookie("refreshToken", "refresh"))
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getCookie("refreshToken").getValue()).isEqualTo("");

        verify(refreshTokenService).revokeToken("refresh");
        verify(tokenBlacklistService).blacklist(eq("jti-123"), any(Long.class));
    }

    @Test
    void logout_withException_stillClearsCookie() throws Exception {
        when(jwtService.extractJti("access")).thenThrow(new RuntimeException("invalid"));

        var result = mockMvcTester
                .post()
                .uri("/api/auth/logout")
                .header("Authorization", "Bearer access")
                .cookie(new Cookie("refreshToken", "refresh"))
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getCookie("refreshToken").getValue()).isEqualTo("");
    }

    @Test
    void verify_missingToken_returns401() throws Exception {
        var result = mockMvcTester.get().uri("/api/auth/verify").exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void verify_validToken_returns200() throws Exception {
        when(jwtService.validateToken("valid")).thenReturn(true);
        when(jwtService.extractUsername("valid")).thenReturn("user1");

        var result = mockMvcTester
                .get()
                .uri("/api/auth/verify")
                .header("Authorization", "Bearer valid")
                .exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.username")
                .asString()
                .isEqualTo("user1");

        assertThat(result).bodyJson().extractingPath("$.valid").asBoolean().isEqualTo(true);
    }

    @Test
    void verify_invalidToken_returns401() throws Exception {
        when(jwtService.validateToken("invalid")).thenReturn(false);

        var result = mockMvcTester
                .get()
                .uri("/api/auth/verify")
                .header("Authorization", "Bearer invalid")
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
