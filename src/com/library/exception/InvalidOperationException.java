package com.library.exception;

/** Thrown for miscellaneous invalid operations, e.g. duplicate username on registration. */
public class InvalidOperationException extends LibraryException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
