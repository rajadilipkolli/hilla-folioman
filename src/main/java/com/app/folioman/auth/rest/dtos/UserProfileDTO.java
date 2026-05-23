package com.app.folioman.auth.rest.dtos;

import java.io.Serializable;

public record UserProfileDTO(String username, String firstname, String lastname, String email, PortfoliosDTO portfolios)
        implements Serializable {}
