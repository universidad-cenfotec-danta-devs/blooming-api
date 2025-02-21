package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record QuestionRequest(@NotNull(message = "question is required") String question) {
}
