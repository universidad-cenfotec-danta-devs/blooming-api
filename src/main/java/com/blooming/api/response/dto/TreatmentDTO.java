package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TreatmentDTO {

    private List<String> chemical;
    private List<String> biological;
    private List<String> prevention;

    @Override
    public String toString() {
        return "TreatmentDTO{" +
                "chemical=" + chemical +
                ", biological=" + biological +
                ", prevention=" + prevention +
                '}';
    }
}
