package com.blooming.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

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
    private String imageURL;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "watering_plan_id", nullable = false)
    private WateringPlan wateringPlan;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public WateringDay(int day, int month, int year, String recommendation) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.recommendation = recommendation;
    }

    public WateringDay() {
    }
}
