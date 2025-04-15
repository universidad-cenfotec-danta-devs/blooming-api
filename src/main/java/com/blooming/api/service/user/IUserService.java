package com.blooming.api.service.user;

import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    ResponseEntity<?> register(User user, RoleEnum role);

    User updateUserProfile(String userEmail,
                           String name,
                           Date dateOfBirth,
                           String gender,
                           String profileImageUrl);

    Optional<User> findByEmail(String email);
    List<User> getNurseryUsers();
}
