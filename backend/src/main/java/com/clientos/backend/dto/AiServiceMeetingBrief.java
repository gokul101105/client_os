package com.clientos.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiServiceMeetingBrief(
        @JsonProperty("agenda_suggestions") List<String> agendaSuggestions,
        @JsonProperty("key_context") String keyContext,
        @JsonProperty("open_issues_to_address") List<String> openIssuesToAddress,
        @JsonProperty("risks_or_watchouts") List<String> risksOrWatchouts
) {
}
