package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WateringPlanDTO {

    private Long id;
    private Long plantId;
    private boolean isActive;

    public WateringPlanDTO(Long id, Long plantId, boolean isActive) {
        this.id = id;
        this.plantId = plantId;
        this.isActive = isActive;
    }
}
