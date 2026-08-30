package com.library.exception;

/** Thrown when a login attempt uses a wrong username/password combination. */
public class InvalidCredentialsException extends LibraryException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
