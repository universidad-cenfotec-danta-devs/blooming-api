package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NurseryDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String name;
    private boolean isActive;
}
