package com.blooming.api.response;

import com.blooming.api.entity.User;
import lombok.Builder;

import java.util.Optional;

@Builder
public record LogInResponse(String token, long expiresIn, Optional<User> authUser) {
}

