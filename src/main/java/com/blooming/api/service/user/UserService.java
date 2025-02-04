package com.blooming.api.service.user;

import com.blooming.api.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class UserService implements IUserService {

    @Override
    public ResponseEntity<?> register(User user) {
        return null;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }
}
