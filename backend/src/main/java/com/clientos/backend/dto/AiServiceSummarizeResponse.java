package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Mirrors ai-service/app/schemas.py's SummarizeResponse field for field.
public record AiServiceSummarizeResponse(
        @JsonProperty("client_id") Long clientId,
        String company,
        @JsonProperty("current_situation") String currentSituation,
        @JsonProperty("major_problems") List<String> majorProblems,
        @JsonProperty("recent_activity") String recentActivity,
        String sentiment,
        @JsonProperty("attention_required") Boolean attentionRequired,
        @JsonProperty("attention_reason") String attentionReason
) {
}
