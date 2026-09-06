package com.clientos.backend.exception;

// Covers everything that can go wrong calling the AI service itself:
// unreachable, timed out, or returned an error status. Never thrown for
// authorization failures — those are ClientNotFoundException, raised
// before the AI service is ever contacted.
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
