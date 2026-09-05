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
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column
    private String industry;

    @Column(nullable = false)
    private String plan;

    @Column(name = "health_score")
    private Integer healthScore;

    @Generated(event = EventType.INSERT)
    @Column(name = "open_issues_count", insertable = false, updatable = false)
    private Integer openIssuesCount;

    @Column(name = "last_activity_date", insertable = false, updatable = false)
    private LocalDateTime lastActivityDate;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Client() {
        // required by JPA
    }

    public Client(String name, User owner, String industry, String plan) {
        this.name = name;
        this.owner = owner;
        this.industry = industry;
        this.plan = plan;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
