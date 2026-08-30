package com.library.exception;

/** Thrown when an operation references an ISBN that is not in the catalog. */
public class BookNotFoundException extends LibraryException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
