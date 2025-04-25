package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotNull(message = "Name cannot be null") String name,
        @NotNull(message = "Description cannot be null") String description,
        @NotNull(message = "Price cannot be null") Double price,
        String userEmail) {
}