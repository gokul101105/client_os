package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Wire shape ai-service/app/schemas.py's SummarizeRequest expects.
// client_name/industry/plan are sent explicitly rather than having Python
// look them up itself -- Python never queries the clients table directly,
// the same boundary that's held since Module 10 (it only ever touches
// document_chunks).
public record AiServiceSummarizeRequest(
        @JsonProperty("client_id") Long clientId,
        @JsonProperty("client_name") String clientName,
        String industry,
        String plan
) {
}
