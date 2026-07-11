package com.app.folioman.auth.domain;

import com.app.folioman.auth.AuthAPI;
import com.app.folioman.auth.CustomUserDetails;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class AuthAPIImpl implements AuthAPI {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    AuthAPIImpl(UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public Optional<CustomUserDetails> findUserDetailsByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToUserDetails);
    }

    @Override
    public Optional<CustomUserDetails> findUserDetailsById(Long id) {
        return userRepository.findById(id).map(this::mapToUserDetails);
    }

    private CustomUserDetails mapToUserDetails(UserEntity userEntity) {
        List<SimpleGrantedAuthority> authorityList = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return new CustomUserDetails(
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                userEntity.isEnabled(),
                true,
                true,
                !userEntity.isAccountLocked(),
                authorityList,
                userEntity.getEmail(),
                userEntity.getId());
    }

    @Override
    @Transactional
    public void createRefreshToken(Long userId, String token) {
        refreshTokenService.createRefreshToken(userId, token);
    }

    @Override
    public boolean isTokenValidAndExists(String token) {
        return refreshTokenService
                .findByToken(token)
                .map(refreshTokenService::isTokenValid)
                .orElse(false);
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenService.revokeToken(token);
    }

    @Override
    public Optional<Long> getUserIdByRefreshToken(String token) {
        return refreshTokenService.findByToken(token).map(RefreshTokenEntity::getUserId);
    }
}
