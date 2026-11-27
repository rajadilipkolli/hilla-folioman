package com.app.folioman.mfschemes;

import java.time.LocalDate;

public class NavNotFoundException extends RuntimeException {

    private final LocalDate navDate;

    public NavNotFoundException(String message, LocalDate navDate) {
        super(message + " on " + navDate.toString());
        this.navDate = navDate;
    }

    public LocalDate getNavDate() {
        return navDate;
    }
}
