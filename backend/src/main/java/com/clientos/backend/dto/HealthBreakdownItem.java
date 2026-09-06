package com.clientos.backend.dto;

// One line of "why is the score what it is" -- signal name, its point
// adjustment (can be negative), and a human-readable reason. Stored as
// part of client_health.breakdown (JSONB) and returned as-is in the API.
public record HealthBreakdownItem(String signal, int points, String reason) {
}
