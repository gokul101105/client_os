package com.clientos.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "message is required") String message
) {
}
