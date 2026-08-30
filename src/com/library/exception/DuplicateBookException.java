package com.library.exception;

/** Thrown when the admin tries to add a book whose ISBN already exists. */
public class DuplicateBookException extends LibraryException {

    public DuplicateBookException(String message) {
        super(message);
    }
}
