package com.clientos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "ownerId is required") Long ownerId
) {
}
