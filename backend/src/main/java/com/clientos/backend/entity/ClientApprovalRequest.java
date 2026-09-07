package com.clientos.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

// One row covers both CREATE and DELETE requests: CREATE populates the
// snapshot_* columns (the proposed client, not yet real), DELETE populates
// targetClient instead. Whichever type a row is, the other half's columns
// stay null -- see database/migrations/V6 for why a single flat table was
// chosen over two, or a subclass hierarchy, for this.
@Entity
@Table(name = "client_requests")
public class ClientApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "requested_at", insertable = false, updatable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "decision_note")
    private String decisionNote;

    @Column(name = "snapshot_name")
    private String snapshotName;

    @Column(name = "snapshot_industry")
    private String snapshotIndustry;

    @Column(name = "snapshot_plan")
    private String snapshotPlan;

    // Nulled out by ON DELETE SET NULL if the client this pointed at is
    // later deleted (which only happens by approving this very request) --
    // the row itself is kept for the audit trail, just without a live FK.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_client_id")
    private Client targetClient;

    protected ClientApprovalRequest() {
        // required by JPA
    }

    public static ClientApprovalRequest forCreate(User requestedBy, String name, String industry, String plan) {
        ClientApprovalRequest request = new ClientApprovalRequest();
        request.type = ClientRequestType.CREATE;
        request.status = ClientRequestStatus.PENDING;
        request.requestedBy = requestedBy;
        request.snapshotName = name;
        request.snapshotIndustry = industry;
        request.snapshotPlan = plan;
        return request;
    }

    public static ClientApprovalRequest forDelete(User requestedBy, Client targetClient) {
        ClientApprovalRequest request = new ClientApprovalRequest();
        request.type = ClientRequestType.DELETE;
        request.status = ClientRequestStatus.PENDING;
        request.requestedBy = requestedBy;
        request.targetClient = targetClient;
        return request;
    }

    // Called right before the target client is actually deleted. Nulling
    // this side of the association in Java first (matching what the DB's
    // ON DELETE SET NULL does anyway) avoids Hibernate trying to reconcile
    // this row's own pending UPDATE (from approve()) against a same-flush
    // DELETE of the row it still points to -- which otherwise fails with
    // "references an unsaved transient instance" at flush time.
    public void clearTargetClient() {
        this.targetClient = null;
    }

    public void approve(User decidedBy, String decisionNote) {
        this.status = ClientRequestStatus.APPROVED;
        this.decidedBy = decidedBy;
        this.decidedAt = LocalDateTime.now();
        this.decisionNote = decisionNote;
    }

    public void reject(User decidedBy, String decisionNote) {
        this.status = ClientRequestStatus.REJECTED;
        this.decidedBy = decidedBy;
        this.decidedAt = LocalDateTime.now();
        this.decisionNote = decisionNote;
    }

    public Long getId() {
        return id;
    }

    public ClientRequestType getType() {
        return type;
    }

    public ClientRequestStatus getStatus() {
        return status;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public User getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public String getSnapshotIndustry() {
        return snapshotIndustry;
    }

    public String getSnapshotPlan() {
        return snapshotPlan;
    }

    public Client getTargetClient() {
        return targetClient;
    }
}
