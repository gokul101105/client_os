package com.clientos.backend.dto;

import java.util.List;

// Ephemeral, like chat (Module 12) -- not persisted. Unlike Module 13/14,
// nothing in this module's spec asked for a recommendations history
// table, and "what should I do right now" is naturally a point-in-time
// question, not a record worth keeping across regenerations.
public record ClientRecommendationsResponse(List<RecommendationItem> recommendations) {
}
