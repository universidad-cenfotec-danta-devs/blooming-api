package com.blooming.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PlantIdentified {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageURL;
    @Column(length = 1000)
    private String watering;
    @Column(length = 1000)
    private String bestWatering;
    @Column(length = 1000)
    private String bestLightCondition;
    @Column(length = 1000)
    private String bestSoilType;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
