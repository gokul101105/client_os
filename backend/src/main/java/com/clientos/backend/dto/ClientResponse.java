package com.clientos.backend.dto;

import com.clientos.backend.entity.Client;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        Long ownerId,
        Integer healthScore,
        LocalDateTime createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getOwnerId(),
                client.getHealthScore(),
                client.getCreatedAt()
        );
    }
}
