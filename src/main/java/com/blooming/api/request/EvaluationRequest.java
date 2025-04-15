package com.blooming.api.request;

import jakarta.validation.constraints.NotNull;

public record EvaluationRequest(String userEmail,
                                @NotNull(message = "objToEvaluateId is required") Long objToEvaluateId,
                                @NotNull(message = "rating is required") int rating,
                                String comment) {
}
