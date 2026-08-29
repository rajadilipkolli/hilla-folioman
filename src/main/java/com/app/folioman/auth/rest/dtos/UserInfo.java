package com.app.folioman.auth.rest.dtos;

import java.util.List;

public record UserInfo(String username, List<String> roles) {}
