package com.clientos.backend.exception;

// Covers everything wrong with an upload attempt itself: empty file,
// disallowed type, oversized file, or a storage I/O failure.
public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
