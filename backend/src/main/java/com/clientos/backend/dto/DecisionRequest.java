package com.clientos.backend.dto;

// Body for both approve and reject -- decisionNote is optional either way,
// so the request body itself is optional too (see the controller methods).
public record DecisionRequest(String decisionNote) {
}
