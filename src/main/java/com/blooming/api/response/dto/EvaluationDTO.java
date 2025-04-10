package com.blooming.api.response.dto;

public record EvaluationDTO(
        Integer rating,
        String comment,
        String createdAt,
        String userName
) {
}
