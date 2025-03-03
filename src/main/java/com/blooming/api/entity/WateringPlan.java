package com.blooming.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
public class WateringPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantIdentified plant;

    @OneToMany(mappedBy = "wateringPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WateringDay> wateringDays;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public WateringPlan() {
    }

    public WateringPlan(List<WateringDay> wateringDays, PlantIdentified plant) {
        this.wateringDays = wateringDays;
        this.plant = plant;
        for (WateringDay day : wateringDays) {
            day.setWateringPlan(this);
        }
    }
}
