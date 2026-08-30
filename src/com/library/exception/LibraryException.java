package com.library.exception;

/**
 * Base checked exception for every custom exception raised inside the
 * Library Management System. Catching this type in the UI layer is
 * enough to handle any library-specific error uniformly.
 */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }
}
