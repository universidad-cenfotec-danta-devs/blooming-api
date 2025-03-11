package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DiseaseDetailsDTO {

    private String localName;
    private String description;
    private String url;
    private TreatmentDTO treatment;

    @Override
    public String toString() {
        return "DiseaseDetailsDTO{" +
                "localName='" + localName + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", treatment=" + treatment +
                '}';
    }
}

