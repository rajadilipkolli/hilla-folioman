package com.app.folioman.auth.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.app.folioman.auth.rest.dtos.UserInfo;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class UserInfoServiceTest {

    private UserInfoService userInfoService;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        userInfoService = new UserInfoService();
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void getUserInfo_unauthenticated_returnsNull() {
        assertThat(userInfoService.getUserInfo()).isNull();
    }

    @Test
    void getUserInfo_authenticatedAsAnonymous_returnsNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(userInfoService.getUserInfo()).isNull();
    }

    @Test
    void getUserInfo_authenticated_returnsUserInfo() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("user");
        when(auth.getName()).thenReturn("testuser");

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ADMIN"));
        // Cast is required for mocking raw type vs wildcard
        when(auth.getAuthorities()).thenReturn((Collection) authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserInfo info = userInfoService.getUserInfo();

        assertThat(info).isNotNull();
        assertThat(info.username()).isEqualTo("testuser");
        assertThat(info.roles()).containsExactly("USER", "ADMIN");
    }
}
