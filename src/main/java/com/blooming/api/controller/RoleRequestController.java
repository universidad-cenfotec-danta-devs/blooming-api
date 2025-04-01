package com.blooming.api.controller;

import com.blooming.api.entity.*;
import com.blooming.api.repository.role.IRoleRepository;
import com.blooming.api.repository.roleRequest.IRoleRequestRepository;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.response.http.GlobalResponseHandler;
import com.blooming.api.response.http.MetaResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/requests")
public class RoleRequestController {
    @Autowired
    private IRoleRequestRepository roleRequestRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getRoleRequestsPaginated(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<RoleRequest> roleRequestPage = roleRequestRepository.findAll(pageable);

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(roleRequestPage.getTotalPages());
        meta.setTotalElements(roleRequestPage.getTotalElements());
        meta.setPageNumber(roleRequestPage.getNumber() + 1);
        meta.setPageSize(roleRequestPage.getSize());

        return new GlobalResponseHandler()._handleResponse("Role Request retrieved successfully", roleRequestPage.getContent(), HttpStatus.OK, meta);
    }

    @PutMapping("/approve/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public User approveRequest(@PathVariable Long userId, @RequestBody RoleRequest roleRequest) {
        Role role = roleRepository.findByName(RoleEnum.valueOf(roleRequest.getRoleRequested()))
                .orElseThrow(() -> new EntityNotFoundException("Role not found."));

        return userRepository.findById(userId).map(user -> {
            user.setRole(role);

            RoleRequest existingRoleRequest = roleRequestRepository.findById(roleRequest.getId())
                    .orElseThrow(() -> new EntityNotFoundException("RoleRequest with id " + roleRequest.getId() + " not found"));

            existingRoleRequest.setRequestStatus(RoleRequestEnum.APPROVED);

            roleRequestRepository.save(existingRoleRequest);

            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
    }




    @PutMapping("/deny/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public User denyRequest(@PathVariable Long userId, @RequestBody RoleRequest roleRequest) {
        Role role = roleRepository.findByName(RoleEnum.SIMPLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found."));

        return userRepository.findById(userId).map(user -> {
            user.setRole(role);
            RoleRequest existingRoleRequest = roleRequestRepository.findById(roleRequest.getId())
                    .orElseThrow(() -> new EntityNotFoundException("RoleRequest with id " + roleRequest.getId() + " not found"));

            existingRoleRequest.setRequestStatus(RoleRequestEnum.DENIED);

            roleRequestRepository.save(existingRoleRequest);

            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<Map<String, String>> deleteRoleRequest(@PathVariable Long id) {
        return roleRequestRepository.findById(id)
                .map(roleRequest -> {
                    roleRequestRepository.delete(roleRequest);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Role request deleted successfully");

                    return ResponseEntity.ok(response);
                }).orElseThrow(() -> new EntityNotFoundException("Role request with " + id + " not found"));
    }
}
