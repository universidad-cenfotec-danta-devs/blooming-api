package com.blooming.api.service.user;

import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IUserService {
    ResponseEntity<?> register(User user, RoleEnum role);

    Optional<User> findByEmail(String email);
}
