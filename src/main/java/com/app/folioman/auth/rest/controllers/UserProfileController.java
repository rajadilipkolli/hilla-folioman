package com.app.folioman.auth.rest.controllers;

import com.app.folioman.auth.domain.UserProfileService;
import com.app.folioman.auth.rest.dtos.UserProfileDTO;
import com.app.folioman.config.redis.CacheNames;
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

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    @Cacheable(cacheNames = CacheNames.USER_PROFILE_CACHE, key = "#principal.name")
    public UserProfileDTO getCurrentUserProfile(Principal principal) {
        return userProfileService.getCurrentUserProfile(principal.getName());
    }
}
