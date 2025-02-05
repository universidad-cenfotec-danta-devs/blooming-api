package com.blooming.api.response;

import lombok.Builder;

@Builder
public record LogInResponse(String token, long expiresIn) {}

