package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WateringDayDTO {
    private final int day;
    private final int month;
    private final int year;
    private final String recommendation;

    public WateringDayDTO(int day, int month, int year, String recommendation) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.recommendation = recommendation;
    }
}
