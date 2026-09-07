package com.clientos.backend.dto;

import com.clientos.backend.entity.ClientApprovalRequest;

import java.time.LocalDateTime;

public record ClientApprovalRequestResponse(
        Long id,
        String type,
        String status,
        Long requestedByUserId,
        String requestedByName,
        LocalDateTime requestedAt,
        Long decidedByUserId,
        String decidedByName,
        LocalDateTime decidedAt,
        String decisionNote,
        String snapshotName,
        String snapshotIndustry,
        String snapshotPlan,
        Long targetClientId
) {
    public static ClientApprovalRequestResponse from(ClientApprovalRequest r) {
        return new ClientApprovalRequestResponse(
                r.getId(),
                r.getType().name(),
                r.getStatus().name(),
                r.getRequestedBy().getId(),
                r.getRequestedBy().getName(),
                r.getRequestedAt(),
                r.getDecidedBy() != null ? r.getDecidedBy().getId() : null,
                r.getDecidedBy() != null ? r.getDecidedBy().getName() : null,
                r.getDecidedAt(),
                r.getDecisionNote(),
                r.getSnapshotName(),
                r.getSnapshotIndustry(),
                r.getSnapshotPlan(),
                r.getTargetClient() != null ? r.getTargetClient().getId() : null
        );
    }
}
