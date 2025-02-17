package com.blooming.api.response.dto;

import lombok.Data;

@Data
public class PlantSuggestionDTO {
    private String name;
    private String idAccessToken;
    private String probabilityPercentage;
    private String imageUrl;
    private String imageUrlSmall;
    private String similarityPercentage;
}