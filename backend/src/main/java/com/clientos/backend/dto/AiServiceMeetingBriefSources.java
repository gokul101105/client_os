package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiServiceMeetingBriefSources(
        @JsonProperty("documents_reviewed") Integer documentsReviewed,
        @JsonProperty("issues_data_available") Boolean issuesDataAvailable,
        @JsonProperty("meetings_data_available") Boolean meetingsDataAvailable
) {
}
