package com.app.folioman.auth.rest.controllers;

import com.app.folioman.auth.UserProfileQuery;
import com.app.folioman.auth.rest.dtos.UserProfileDTO;
import com.app.folioman.shared.CacheNames;
import jakarta.annotation.security.RolesAllowed;
import java.security.Principal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RolesAllowed("USER")
public class UserProfileController {

    private final UserProfileQuery userProfileQuery;

    public UserProfileController(UserProfileQuery userProfileQuery) {
        this.userProfileQuery = userProfileQuery;
    }

    @GetMapping
    @Cacheable(cacheNames = CacheNames.USER_PROFILE_CACHE, key = "#principal.name")
    public UserProfileDTO getCurrentUserProfile(Principal principal) {
        return userProfileQuery.getCurrentUserProfile(principal.getName());
    }
}
