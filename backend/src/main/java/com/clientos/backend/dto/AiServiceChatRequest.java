package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// The wire shape the AI service's Pydantic ChatRequest actually expects
// (see ai-service/app/schemas.py) — snake_case, distinct from the
// camelCase AiChatRequest React sends us. Never exposed outside
// AiServiceClient.
public record AiServiceChatRequest(
        @JsonProperty("client_id") Long clientId,
        String message,
        @JsonProperty("conversation_id") String conversationId
) {
}
