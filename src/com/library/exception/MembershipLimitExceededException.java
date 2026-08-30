package com.library.exception;

/** Thrown when a member tries to borrow more books than their allowed limit. */
public class MembershipLimitExceededException extends LibraryException {

    public MembershipLimitExceededException(String message) {
        super(message);
    }
}
