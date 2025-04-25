package com.blooming.api.controller;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.repository.role.IRoleRepository;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.request.UserProfileUpdateRequest;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.GlobalResponseHandler;
import com.blooming.api.response.http.MetaResponse;
import com.blooming.api.service.s3.IS3Service;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IS3Service s3Service;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        return userService.register(user, RoleEnum.SIMPLE_USER);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'ROLE_SIMPLE_USER', 'NURSERY_USER')")
    @PatchMapping("/updateProfile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest userProfileUpdateRequest,
                                               HttpServletRequest request) {

        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        try {
            User updated = userService.updateUserProfile(userEmail,
                    userProfileUpdateRequest.name(),
                    userProfileUpdateRequest.dateOfBirth(),
                    userProfileUpdateRequest.gender());
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    updated,
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.name(),
                    "Error processing request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getAllUsersPaginated(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> usersPage = userRepository.findAll(pageable);

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler()._handleResponse("User retrieved successfully", usersPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setDateOfBirth(updatedUser.getDateOfBirth());
            user.setGender(updatedUser.getGender());

            if (updatedUser.getRole() != null && updatedUser.getRole().getName() != null) {
                Role role = roleRepository.findByName(updatedUser.getRole().getName())
                        .orElseThrow(() -> new EntityNotFoundException("Role not found"));
                user.setRole(role);
            }

            user.setActive(updatedUser.isActive());
            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "User deleted successfully");

                    return ResponseEntity.ok(response);
                })
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }
}
