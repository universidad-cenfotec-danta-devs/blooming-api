package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record EvaluationRequest(@NotNull(message = "objToEvaluateId is required") Long objToEvaluateId,
                                @NotNull(message = "rating is required") int rating,
                                @NotNull(message = "anonymous or not is required") boolean anonymous,
                                String comment) {
}
