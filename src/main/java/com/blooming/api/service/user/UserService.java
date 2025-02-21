package com.blooming.api.service.user;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.repository.role.IRoleRepository;
import com.blooming.api.repository.user.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepository, IRoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ResponseEntity<?> register(User user, RoleEnum rolAssigned) {

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> role = roleRepository.findByName(rolAssigned);

        if (role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }
        user.setRole(role.get());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
