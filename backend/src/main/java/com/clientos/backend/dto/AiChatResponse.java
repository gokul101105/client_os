package com.clientos.backend.dto;

import java.util.List;

public record AiChatResponse(
        String reply,
        List<Long> sourceChunkIds
) {
    public static AiChatResponse from(AiServiceChatResponse response) {
        return new AiChatResponse(response.reply(), response.sourceChunkIds());
    }
}
