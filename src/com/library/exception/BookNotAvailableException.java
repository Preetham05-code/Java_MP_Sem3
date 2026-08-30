package com.library.exception;

/** Thrown when a member tries to borrow a book that has no copies left. */
public class BookNotAvailableException extends LibraryException {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
