package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Mirrors the AI service's Pydantic ChatResponse (ai-service/app/schemas.py)
// field for field. Never exposed outside AiServiceClient — AiAssistantService
// maps this into the public AiChatResponse before it reaches a controller.
public record AiServiceChatResponse(
        String reply,
        @JsonProperty("conversation_id") String conversationId,
        @JsonProperty("source_chunk_ids") List<Long> sourceChunkIds
) {
}
