package com.clientos.backend.dto;

import com.clientos.backend.entity.ClientSummary;

import java.time.LocalDateTime;
import java.util.List;

public record ClientSummaryResponse(
        Long id,
        String company,
        String currentSituation,
        List<String> majorProblems,
        String recentActivity,
        String sentiment,
        Boolean attentionRequired,
        String attentionReason,
        String generatedByName,
        LocalDateTime generatedAt
) {
    public static ClientSummaryResponse from(ClientSummary summary) {
        return new ClientSummaryResponse(
                summary.getId(),
                summary.getCompany(),
                summary.getCurrentSituation(),
                summary.getMajorProblems(),
                summary.getRecentActivity(),
                summary.getSentiment(),
                summary.getAttentionRequired(),
                summary.getAttentionReason(),
                summary.getGeneratedBy().getName(),
                summary.getGeneratedAt()
        );
    }
}
