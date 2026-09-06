package com.clientos.backend.dto;

import com.clientos.backend.entity.ClientHealth;
import com.clientos.backend.health.HealthBand;

import java.time.LocalDateTime;
import java.util.List;

public record ClientHealthResponse(
        Integer score,
        String band,
        String emoji,
        List<HealthBreakdownItem> breakdown,
        LocalDateTime computedAt
) {
    public static ClientHealthResponse from(ClientHealth health) {
        HealthBand band = HealthBand.valueOf(health.getBand());
        return new ClientHealthResponse(
                health.getScore(),
                band.getLabel(),
                band.getEmoji(),
                health.getBreakdown(),
                health.getComputedAt()
        );
    }
}
