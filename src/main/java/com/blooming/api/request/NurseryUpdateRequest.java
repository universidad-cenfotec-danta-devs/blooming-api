package com.blooming.api.request;

public record NurseryUpdateRequest(String name,
                                   String description,
                                   double latitude,
                                   double longitude) {
}