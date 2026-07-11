package com.app.folioman.auth.domain;

import com.app.folioman.auth.CustomUserDetails;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorityList = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        if (authorityList.isEmpty()) {
            throw new UsernameNotFoundException("User has no roles assigned: " + username);
        }

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
