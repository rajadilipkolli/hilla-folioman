package com.app.folioman.auth;

import com.app.folioman.auth.rest.dtos.UserProfileDTO;

public interface UserProfileQuery {

    UserProfileDTO getCurrentUserProfile(String username);
}
