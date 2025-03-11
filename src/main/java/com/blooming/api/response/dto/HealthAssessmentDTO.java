package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HealthAssessmentDTO {

    private List<DiseaseSuggestionDTO> diseaseSuggestions;

    @Override
    public String toString() {
        return "HealthAssessmentDTO{" +
                "diseaseSuggestions=" + diseaseSuggestions +
                '}';
    }
}
