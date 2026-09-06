package com.clientos.backend.health;

import com.clientos.backend.dto.HealthBreakdownItem;

import java.util.List;

public record HealthScoreResult(int score, HealthBand band, List<HealthBreakdownItem> breakdown) {
}
