package com.blooming.api.service.user;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.RoleRequest;
import com.blooming.api.entity.User;
import com.blooming.api.repository.role.IRoleRepository;
import com.blooming.api.repository.roleRequest.IRoleRequestRepository;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.service.roleRequest.RoleRequestService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRequestService roleRequestService;

    public UserService(IUserRepository userRepository, IRoleRepository roleRepository, PasswordEncoder passwordEncoder, RoleRequestService roleRequestService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRequestService = roleRequestService;
    }

    @Override
    @Transactional
    public ResponseEntity<?> register(User user, RoleEnum rolAssigned) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> role = roleRepository.findByName(rolAssigned);

        if (role.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Role not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        user.setRole(role.get());
        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        if (!rolAssigned.name().equals("SIMPLE_USER")) {
            RoleRequest roleRequest = new RoleRequest();
            roleRequest.setRoleRequested(rolAssigned.name());
            roleRequest.setRequesterId(savedUser.getId());
            roleRequest.setRequesterEmail(savedUser.getEmail());
            roleRequest.setRequestStatus(false);

            roleRequestService.addRoleRequest(roleRequest);
        }
        return ResponseEntity.ok(savedUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
