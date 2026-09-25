package com.app.folioman.shared;

import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public final class PrincipalUtil {

    private PrincipalUtil() {}

    public static String getEmailFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "";
        }
        return extractUserEmail(auth);
    }

    public static String extractUserEmail(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object authPrincipal = authentication.getPrincipal();
            if (authPrincipal instanceof UserDetails userDetails) {
                if (authPrincipal instanceof EmailAware emailAware) {
                    return emailAware.getEmail();
                }
                return userDetails.getUsername();
            }
            if (authPrincipal instanceof OidcUser oidcUser) {
                String email = oidcUser.getEmail();
                return email != null ? email : "";
            }
            if (authPrincipal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                return email != null ? email : "";
            }
        }
        return principal.getName() != null ? principal.getName() : "";
    }
}
