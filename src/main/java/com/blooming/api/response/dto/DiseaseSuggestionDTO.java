package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiseaseSuggestionDTO {

    private String id;
    private String name;
    private double probability;
    private DiseaseDetailsDTO details;

    @Override
    public String toString() {
        return "DiseaseSuggestionDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", probability=" + probability +
                ", details=" + details +
                '}';
    }
}

