package com.app.folioman.mfschemes;

import java.time.LocalDate;

public class NavNotFoundException extends RuntimeException {

    private final LocalDate date;

    public NavNotFoundException(String message, LocalDate date) {
        super(message + " on " + date.toString());
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}
