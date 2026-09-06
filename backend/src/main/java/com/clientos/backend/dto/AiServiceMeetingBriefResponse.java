package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiServiceMeetingBriefResponse(
        @JsonProperty("client_id") Long clientId,
        AiServiceMeetingBrief brief,
        @JsonProperty("sources_used") AiServiceMeetingBriefSources sourcesUsed
) {
}
