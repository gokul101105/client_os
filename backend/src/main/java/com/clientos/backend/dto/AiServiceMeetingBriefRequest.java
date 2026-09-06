package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Wire shape ai-service/app/schemas.py's MeetingBriefRequest expects.
public record AiServiceMeetingBriefRequest(
        @JsonProperty("client_id") Long clientId,
        @JsonProperty("client_name") String clientName,
        String industry,
        String plan,
        @JsonProperty("open_issues_count") Integer openIssuesCount
) {
}
