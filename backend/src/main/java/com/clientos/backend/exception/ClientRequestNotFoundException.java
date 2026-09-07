package com.clientos.backend.exception;

public class ClientRequestNotFoundException extends RuntimeException {

    public ClientRequestNotFoundException(Long id) {
        super("Client request not found with id: " + id);
    }
}
