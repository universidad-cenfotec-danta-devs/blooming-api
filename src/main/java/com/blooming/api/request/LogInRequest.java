package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LogInRequest(@NotNull(message = "email is required") String email,
                           @NotNull(message = "password is required") String password) {
}
