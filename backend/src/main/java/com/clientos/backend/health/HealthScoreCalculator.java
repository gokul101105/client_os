package com.clientos.backend.health;

import com.clientos.backend.dto.HealthBreakdownItem;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Pure, deterministic, rule-based scoring -- no LLM call, no I/O. See the
// Module 14 write-up for why rule-based (explainability, no training
// data, human-tunable) and why this lives in Java (every input already
// lives in tables Spring Boot owns).
@Component
public class HealthScoreCalculator {

    private static final int BASE_SCORE = 100;
    private static final int POINTS_PER_OPEN_ISSUE = -5;
    private static final int MAX_OPEN_ISSUE_PENALTY = -30;

    public HealthScoreResult calculate(
            Integer openIssuesCount,
            LocalDateTime lastDocumentActivity,
            String latestSentiment,
            Boolean latestAttentionRequired
    ) {
        List<HealthBreakdownItem> breakdown = new ArrayList<>();
        int total = BASE_SCORE;

        total += applyOpenIssues(openIssuesCount, breakdown);
        total += applyDocumentActivity(lastDocumentActivity, breakdown);
        total += applySentiment(latestSentiment, breakdown);
        total += applyAttentionRequired(latestAttentionRequired, breakdown);

        int clamped = Math.max(0, Math.min(100, total));
        return new HealthScoreResult(clamped, HealthBand.forScore(clamped), breakdown);
    }

    private int applyOpenIssues(Integer openIssuesCountOrNull, List<HealthBreakdownItem> breakdown) {
        // open_issues_count is a real column (Module 5), but nothing in
        // ClientOS increments it yet -- no ticketing module exists. This
        // is honestly a no-op today (every client has 0), kept in the
        // formula so it activates automatically once that module exists.
        int openIssuesCount = openIssuesCountOrNull != null ? openIssuesCountOrNull : 0;
        int points = Math.max(openIssuesCount * POINTS_PER_OPEN_ISSUE, MAX_OPEN_ISSUE_PENALTY);
        String reason = openIssuesCount + " open issue" + (openIssuesCount == 1 ? "" : "s");
        breakdown.add(new HealthBreakdownItem("open_issues", points, reason));
        return points;
    }

    private int applyDocumentActivity(LocalDateTime lastDocumentActivity, List<HealthBreakdownItem> breakdown) {
        int points;
        String reason;
        if (lastDocumentActivity == null) {
            // Not "penalize as inactive" -- a brand-new client with zero
            // uploads yet isn't unhealthy, there's just no signal.
            points = 0;
            reason = "No document activity recorded yet";
        } else {
            long daysSince = Duration.between(lastDocumentActivity, LocalDateTime.now()).toDays();
            if (daysSince <= 7) {
                points = 5;
                reason = "Active in the last 7 days";
            } else if (daysSince <= 30) {
                points = 0;
                reason = "Last activity " + daysSince + " days ago";
            } else if (daysSince <= 90) {
                points = -10;
                reason = "Last activity " + daysSince + " days ago";
            } else {
                points = -20;
                reason = "No activity in over 90 days";
            }
        }
        breakdown.add(new HealthBreakdownItem("document_activity", points, reason));
        return points;
    }

    private int applySentiment(String sentiment, List<HealthBreakdownItem> breakdown) {
        int points;
        String reason;
        if (sentiment == null) {
            points = 0;
            reason = "No client summary generated yet";
        } else {
            points = switch (sentiment) {
                case "Positive" -> 10;
                case "Neutral" -> 0;
                case "Mixed" -> -10;
                case "Negative" -> -20;
                default -> 0;
            };
            reason = "Latest summary sentiment: " + sentiment;
        }
        breakdown.add(new HealthBreakdownItem("sentiment", points, reason));
        return points;
    }

    private int applyAttentionRequired(Boolean attentionRequired, List<HealthBreakdownItem> breakdown) {
        int points;
        String reason;
        if (attentionRequired == null) {
            points = 0;
            reason = "No client summary generated yet";
        } else if (attentionRequired) {
            points = -15;
            reason = "Flagged as needing attention in latest summary";
        } else {
            points = 0;
            reason = "Not flagged for attention";
        }
        breakdown.add(new HealthBreakdownItem("attention_required", points, reason));
        return points;
    }
}
