package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Matches ai-service's ProcessDocumentRequest (see ai-service/app/schemas.py).
public record AiServiceProcessDocumentRequest(
        @JsonProperty("client_id") Long clientId,
        @JsonProperty("document_id") Long documentId,
        @JsonProperty("file_path") String filePath
) {
}
