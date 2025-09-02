package com.markers.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull String name,
        @NotNull String email,
        @NotNull String password) {
}
