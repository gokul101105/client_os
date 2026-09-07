package com.clientos.backend.exception;

public class SelfDeletionNotAllowedException extends RuntimeException {

    public SelfDeletionNotAllowedException() {
        super("You cannot delete your own account");
    }
}
