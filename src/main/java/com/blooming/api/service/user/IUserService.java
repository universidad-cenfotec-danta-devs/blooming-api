package com.blooming.api.service.user;

import com.blooming.api.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IUserService {
    ResponseEntity<?> register(User user);

    Optional<User> findByEmail(String email);
}
