package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantDetailsDTO {
    private String name;
    private String watering;
    private String bestWatering;
    private String bestLightCondition;
    private String bestSoilType;

}
