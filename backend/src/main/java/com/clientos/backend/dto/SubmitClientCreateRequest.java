package com.clientos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitClientCreateRequest(
        @NotBlank(message = "name is required") @Size(max = 255) String name,
        @Size(max = 100, message = "industry must be at most 100 characters") String industry,
        @Size(max = 50, message = "plan must be at most 50 characters") String plan
) {
}
