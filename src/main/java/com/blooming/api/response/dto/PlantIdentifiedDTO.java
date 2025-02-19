package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlantIdentifiedDTO {
    private Long id;
    private String name;
    private String watering;
    private String bestWatering;
    private String bestLightCondition;
    private String bestSoilType;

    public PlantIdentifiedDTO(Long id,String name, String watering, String bestWatering, String bestLightCondition, String bestSoilType) {
        this.id = id;
        this.name = name;
        this.watering = watering;
        this.bestWatering = bestWatering;
        this.bestLightCondition = bestLightCondition;
        this.bestSoilType = bestSoilType;
    }
}
