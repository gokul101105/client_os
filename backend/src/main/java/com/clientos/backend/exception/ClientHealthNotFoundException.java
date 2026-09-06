package com.clientos.backend.exception;

public class ClientHealthNotFoundException extends RuntimeException {

    public ClientHealthNotFoundException(Long clientId) {
        super("No health score has been computed yet for client: " + clientId);
    }
}
