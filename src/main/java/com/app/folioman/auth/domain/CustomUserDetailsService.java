package com.app.folioman.auth.domain;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String[] roles = userEntity.getRoles().stream().map(RoleEntity::getName).toArray(String[]::new);

        if (roles.length == 0) {
            throw new UsernameNotFoundException("User has no roles assigned: " + username);
        }

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPasswordHash())
                .disabled(!userEntity.isEnabled())
                .accountLocked(userEntity.isAccountLocked())
                .roles(roles)
                .build();
    }
}
