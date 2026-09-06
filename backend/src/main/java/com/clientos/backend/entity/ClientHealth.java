package com.clientos.backend.entity;

import com.clientos.backend.dto.HealthBreakdownItem;
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
@Table(name = "client_health")
public class ClientHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private String band;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<HealthBreakdownItem> breakdown;

    @Generated(event = EventType.INSERT)
    @Column(name = "computed_at", insertable = false, updatable = false)
    private LocalDateTime computedAt;

    protected ClientHealth() {
        // required by JPA
    }

    public ClientHealth(Client client, Integer score, String band, List<HealthBreakdownItem> breakdown) {
        this.client = client;
        this.score = score;
        this.band = band;
        this.breakdown = breakdown;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Integer getScore() {
        return score;
    }

    public String getBand() {
        return band;
    }

    public List<HealthBreakdownItem> getBreakdown() {
        return breakdown;
    }

    public LocalDateTime getComputedAt() {
        return computedAt;
    }
}
