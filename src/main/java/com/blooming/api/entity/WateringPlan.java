package com.blooming.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class WateringPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "wateringPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WateringDay> wateringDays;

    public WateringPlan() {
    }

    public WateringPlan(List<WateringDay> wateringDays) {
        this.wateringDays = wateringDays;
        for (WateringDay day : wateringDays) {
            day.setWateringPlan(this);
        }
    }
}
