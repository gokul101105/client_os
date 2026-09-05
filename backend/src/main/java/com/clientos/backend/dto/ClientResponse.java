package com.clientos.backend.dto;

import com.clientos.backend.entity.Client;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        String industry,
        String plan,
        String accountManagerName,
        Integer healthScore,
        Integer openIssuesCount,
        LocalDateTime lastActivityDate,
        LocalDateTime createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getIndustry(),
                client.getPlan(),
                client.getOwner().getName(),
                client.getHealthScore(),
                client.getOpenIssuesCount(),
                client.getLastActivityDate(),
                client.getCreatedAt()
        );
    }
}
