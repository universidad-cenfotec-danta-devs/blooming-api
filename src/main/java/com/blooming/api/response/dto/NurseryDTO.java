package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class NurseryDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;
    private String userEmail;
}