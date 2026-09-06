package com.clientos.backend.dto;

import java.util.List;

// Public, camelCase shape returned to React. Ephemeral like chat and
// recommendations -- not persisted, since a meeting brief is naturally
// generated fresh right before each meeting rather than kept as history.
public record MeetingBriefResponse(
        List<String> agendaSuggestions,
        String keyContext,
        List<String> openIssuesToAddress,
        List<String> risksOrWatchouts,
        Integer documentsReviewed,
        Boolean issuesDataAvailable,
        Boolean meetingsDataAvailable
) {
    public static MeetingBriefResponse from(AiServiceMeetingBriefResponse response) {
        return new MeetingBriefResponse(
                response.brief().agendaSuggestions(),
                response.brief().keyContext(),
                response.brief().openIssuesToAddress(),
                response.brief().risksOrWatchouts(),
                response.sourcesUsed().documentsReviewed(),
                response.sourcesUsed().issuesDataAvailable(),
                response.sourcesUsed().meetingsDataAvailable()
        );
    }
}
