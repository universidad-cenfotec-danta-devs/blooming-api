package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlantIdentifiedDTO {
    private Long id;
    private String name;
    private boolean isActive;

    public PlantIdentifiedDTO(Long id, String name, boolean isActive) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }
}
