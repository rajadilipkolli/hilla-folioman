package com.app.folioman.auth.domain;

import com.app.folioman.auth.AuthAPI;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthAPI authAPI;

    CustomUserDetailsService(AuthAPI authAPI) {
        this.authAPI = authAPI;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authAPI.findUserDetailsByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or has no roles: " + username));
    }
}
