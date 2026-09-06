package com.clientos.backend.dto;

import com.clientos.backend.entity.Client;
import com.clientos.backend.health.HealthBand;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        String industry,
        String plan,
        String accountManagerName,
        Integer healthScore,
        String healthBand,
        String healthEmoji,
        Integer openIssuesCount,
        LocalDateTime lastActivityDate,
        LocalDateTime createdAt
) {
    public static ClientResponse from(Client client) {
        // Band/emoji are derived from the stored score at read time,
        // rather than also stored on the client row -- one banding
        // implementation (HealthBand.forScore), so a stored band can
        // never drift out of sync with its score.
        HealthBand band = client.getHealthScore() != null ? HealthBand.forScore(client.getHealthScore()) : null;
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getIndustry(),
                client.getPlan(),
                client.getOwner().getName(),
                client.getHealthScore(),
                band != null ? band.getLabel() : null,
                band != null ? band.getEmoji() : null,
                client.getOpenIssuesCount(),
                client.getLastActivityDate(),
                client.getCreatedAt()
        );
    }
}
