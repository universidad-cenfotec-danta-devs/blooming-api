package com.blooming.api.service.user;

import com.blooming.api.entity.User;
import com.blooming.api.repository.IUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<?> register(User user) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userRepository.save(user));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }
}
