package com.blooming.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class WateringDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int day;
    private int month;
    private int year;
    private String recommendation;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "watering_plan_id", nullable = false)
    private WateringPlan wateringPlan;

    public WateringDay(int day, int month, int year, String recommendation) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.recommendation = recommendation;
    }

    public WateringDay() {
    }
}
