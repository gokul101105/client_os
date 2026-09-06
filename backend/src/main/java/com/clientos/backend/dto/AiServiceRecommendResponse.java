package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Mirrors ai-service/app/schemas.py's RecommendResponse.
public record AiServiceRecommendResponse(
        @JsonProperty("client_id") Long clientId,
        List<AiServiceRecommendationItem> recommendations
) {
}
