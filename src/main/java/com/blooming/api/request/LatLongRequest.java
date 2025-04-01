package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record LatLongRequest(@NotNull(message = "latitude is required") double latitude,
                             @NotNull(message = "longitude is required") double longitude) {
}
