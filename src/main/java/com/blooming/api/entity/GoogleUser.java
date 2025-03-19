package com.blooming.api.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GoogleUser {

    private String sub; // User ID
    private String email;
    private String name;
    private String picture;
    private String aud; // Audience, which should match your clientId
}
