package com.blooming.api.request;

import lombok.Data;

@Data
public class UpdateNurseryRequest {
    private Double latitude;
    private Double longitude;
    private String name;
    private Boolean isActive;
}
