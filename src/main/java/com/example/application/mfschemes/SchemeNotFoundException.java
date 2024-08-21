package com.example.application.mfschemes;

public class SchemeNotFoundException extends RuntimeException {

    public SchemeNotFoundException(String message) {
        super(message);
    }
}
