package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record NurseryRequest(@NotNull(message = "name is required") String name,
                             @NotNull(message = "description is required") String description,
                             @NotNull(message = "latitude is required") double latitude,
                             @NotNull(message = "longitude is required") double longitude,
                             String imageUrl,
                             String userEmail) {
}