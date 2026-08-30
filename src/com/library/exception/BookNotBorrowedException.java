package com.library.exception;

/** Thrown when a member tries to return a book they never borrowed. */
public class BookNotBorrowedException extends LibraryException {

    public BookNotBorrowedException(String message) {
        super(message);
    }
}
