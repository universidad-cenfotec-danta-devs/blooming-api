package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlantIdentifiedDTO {
    private Long id;
    private String name;

    public PlantIdentifiedDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
