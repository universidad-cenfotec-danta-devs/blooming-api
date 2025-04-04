package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record PotRequest(@NotNull(message = "name is required") String name,
                         @NotNull(message = "description is required") String description,
                         @NotNull(message = "price is required") double price) {
}
