package com.app.folioman.auth.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.auth.rest.dto.LoginRequest;
import com.app.folioman.shared.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@Execution(ExecutionMode.SAME_THREAD)
class AuthControllerIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM portfolio.refresh_tokens");
        jdbcTemplate.update("DELETE FROM portfolio.user_roles");
        jdbcTemplate.update("DELETE FROM portfolio.users");

        String passwordHash = passwordEncoder.encode("password123");

        jdbcTemplate.update(
                "INSERT INTO portfolio.users (id, username, email, password_hash, enabled, account_locked, failed_login_attempts) "
                        + "VALUES (nextval('portfolio.users_seq'), 'testuser', 'test@test.com', ?, true, false, 0)",
                passwordHash);

        jdbcTemplate.update("INSERT INTO portfolio.user_roles (user_id, role_id) "
                + "SELECT u.id, r.id FROM portfolio.users u, portfolio.roles r "
                + "WHERE u.username = 'testuser' AND r.name = 'USER'");
    }

    @Test
    void successfulLoginReturnsTokens() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void invalidCredentialsReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointIssuesNewTokens() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = result.getResponse().getCookie("refreshToken").getValue();

        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = result.getResponse().getCookie("refreshToken").getValue();

        // Logout
        mockMvc.perform(post("/api/auth/logout").with(csrf()).cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk());

        // Refresh with revoked token should fail
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/nav/123456")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsReturn200WithValidToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = jsonMapper.readValue(responseStr, Map.class);
        String accessToken = (String) responseMap.get("accessToken");

        mockMvc.perform(get("/api/nav/122639").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void bruteForceProtectionLocksAccount() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // 5 failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt with correct password should still fail due to lock
        loginRequest.setPassword("password123");
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Account is locked due to too many failed attempts."));
    }

    @Test
    void logoutBlacklistsAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = jsonMapper.readValue(responseStr, Map.class);
        String accessToken = (String) responseMap.get("accessToken");

        // Authenticated request works
        mockMvc.perform(get("/api/nav/122639").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout").with(csrf()).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Same request should now fail with 401
        mockMvc.perform(get("/api/nav/122639").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyReturns200AndUsernameForValidToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = jsonMapper.readValue(responseStr, Map.class);
        String accessToken = (String) responseMap.get("accessToken");

        mockMvc.perform(get("/api/auth/verify").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void verifyReturns401ForInvalidOrMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/verify").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
