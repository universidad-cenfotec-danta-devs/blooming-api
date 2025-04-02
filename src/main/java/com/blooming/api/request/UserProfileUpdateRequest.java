package com.blooming.api.request;

import jakarta.validation.constraints.Size;

import java.util.Date;

public record UserProfileUpdateRequest(
        @Size(min = 1, message = "Name cannot be empty") String name,
        Date dateOfBirth,
        String gender,
        String profileImageUrl) {
}