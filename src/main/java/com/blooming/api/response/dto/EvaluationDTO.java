package com.blooming.api.response.dto;

public record EvaluationDTO(
        Long id,
        Integer rating,
        String comment,
        String createdAt,
        String userName
) {
}
