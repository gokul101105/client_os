package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Wire shape ai-service/app/schemas.py's RecommendRequest expects. Spring
// Boot assembles every field from data it already owns (Client,
// ClientHealth, ClientSummary) before this is ever sent -- Python never
// queries those tables directly.
public record AiServiceRecommendRequest(
        @JsonProperty("client_id") Long clientId,
        @JsonProperty("client_name") String clientName,
        String industry,
        String plan,
        @JsonProperty("health_score") Integer healthScore,
        @JsonProperty("health_band") String healthBand,
        @JsonProperty("health_breakdown_reasons") List<String> healthBreakdownReasons,
        @JsonProperty("current_situation") String currentSituation,
        @JsonProperty("major_problems") List<String> majorProblems,
        String sentiment,
        @JsonProperty("attention_required") Boolean attentionRequired,
        @JsonProperty("attention_reason") String attentionReason
) {
}
