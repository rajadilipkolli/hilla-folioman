package com.app.folioman.auth;

import com.app.folioman.shared.EmailAware;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Custom user details implementation that extends Spring Security's User
 * to include additional user information such as email and user ID.
 */
public class CustomUserDetails extends User implements EmailAware {

    private final String email;
    private final Long id;

    /**
     * Constructs a CustomUserDetails with full user information.
     *
     * @param username The username
     * @param password The password
     * @param enabled Whether the account is enabled
     * @param accountNonExpired Whether the account is not expired
     * @param credentialsNonExpired Whether the credentials are not expired
     * @param accountNonLocked Whether the account is not locked
     * @param authorities The granted authorities
     * @param email The user's email address
     * @param id The user's unique identifier
     */
    public CustomUserDetails(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities,
            String email,
            Long id) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.email = email;
        this.id = id;
    }

    /**
     * Returns the user's email address.
     *
     * @return The email address
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's unique identifier.
     *
     * @return The user ID
     */
    public Long getId() {
        return id;
    }
}
