package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record LogInRequest(@NotNull(message = "token is required") String email,
                           @NotNull(message = "password is required") String password) {
}
