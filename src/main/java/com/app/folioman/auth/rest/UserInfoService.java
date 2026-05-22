package com.app.folioman.auth.rest;

import com.app.folioman.auth.rest.dtos.UserInfo;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@BrowserCallable
@PermitAll
public class UserInfoService {

    public UserInfo getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String username = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();

        return new UserInfo(username, roles);
    }
}
