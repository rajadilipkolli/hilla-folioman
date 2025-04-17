package com.app.folioman.mfschemes.exception;

/**
 * Exception thrown when there is an error loading mutual fund data.
 */
public class MutualFundDataException extends RuntimeException {

    public MutualFundDataException(String message) {
        super(message);
    }

    public MutualFundDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
