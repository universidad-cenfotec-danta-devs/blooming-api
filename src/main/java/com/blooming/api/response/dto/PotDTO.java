package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class PotDTO {
    Long id;
    Long ownerId;
    String ownerName;
    String name;
    String description;
    double price;
    String fileUrl;
    boolean status;
    Date createdAt;
    Date updatedAt;
}
