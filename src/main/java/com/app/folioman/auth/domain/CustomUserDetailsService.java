package com.app.folioman.auth.domain;

import com.app.folioman.auth.CustomUserDetails;
import org.jspecify.annotations.NonNull;
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

        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorityList =
                java.util.Arrays.stream(roles)
                        .map(role ->
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

        return new CustomUserDetails(
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                userEntity.isEnabled(),
                true,
                true,
                !userEntity.isAccountLocked(),
                authorityList,
                userEntity.getEmail());
    }
}
