package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotNull;

public record FileContentUpdateRequest(@NotNull(message = "Content cannot be null.") String content) {
}