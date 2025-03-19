package com.blooming.api.response;

import com.blooming.api.entity.User;
import lombok.Builder;

@Builder
public record LogInResponse(boolean success,
                            String token,
                            User authUser,
                            long expiresIn) {
}

