package com.clientos.backend.exception;

public class ClientSummaryNotFoundException extends RuntimeException {

    public ClientSummaryNotFoundException(Long clientId) {
        super("No summary has been generated yet for client: " + clientId);
    }
}
