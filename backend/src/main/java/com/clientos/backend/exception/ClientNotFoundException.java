package com.clientos.backend.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long id) {
        super("Client not found with id: " + id);
    }
}
