package com.clientos.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "client_summaries")
public class ClientSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column
    private String company;

    @Column(name = "current_situation", nullable = false, columnDefinition = "text")
    private String currentSituation;

    // Hibernate 6's native JSON mapping -- serializes/deserializes via the
    // configured ObjectMapper, no extra library needed for a JSONB column.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "major_problems", nullable = false, columnDefinition = "jsonb")
    private List<String> majorProblems;

    @Column(name = "recent_activity", nullable = false, columnDefinition = "text")
    private String recentActivity;

    @Column(nullable = false)
    private String sentiment;

    @Column(name = "attention_required", nullable = false)
    private Boolean attentionRequired;

    @Column(name = "attention_reason", columnDefinition = "text")
    private String attentionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;

    protected ClientSummary() {
        // required by JPA
    }

    public ClientSummary(
            Client client,
            String company,
            String currentSituation,
            List<String> majorProblems,
            String recentActivity,
            String sentiment,
            Boolean attentionRequired,
            String attentionReason,
            User generatedBy
    ) {
        this.client = client;
        this.company = company;
        this.currentSituation = currentSituation;
        this.majorProblems = majorProblems;
        this.recentActivity = recentActivity;
        this.sentiment = sentiment;
        this.attentionRequired = attentionRequired;
        this.attentionReason = attentionReason;
        this.generatedBy = generatedBy;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public String getCompany() {
        return company;
    }

    public String getCurrentSituation() {
        return currentSituation;
    }

    public List<String> getMajorProblems() {
        return majorProblems;
    }

    public String getRecentActivity() {
        return recentActivity;
    }

    public String getSentiment() {
        return sentiment;
    }

    public Boolean getAttentionRequired() {
        return attentionRequired;
    }

    public String getAttentionReason() {
        return attentionReason;
    }

    public User getGeneratedBy() {
        return generatedBy;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
