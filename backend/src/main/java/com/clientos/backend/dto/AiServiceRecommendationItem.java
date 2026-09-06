package com.clientos.backend.dto;

// Mirrors ai-service/app/schemas.py's Recommendation.
public record AiServiceRecommendationItem(String action, String reason, String priority) {
}
