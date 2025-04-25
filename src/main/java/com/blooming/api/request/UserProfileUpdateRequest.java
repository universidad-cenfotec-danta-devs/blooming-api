package com.blooming.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record UserProfileUpdateRequest(
        @Size(min = 1, message = "Name cannot be empty") String name,
        @JsonFormat(pattern = "yyyy-MM-dd") Date dateOfBirth,
        String gender) {
}