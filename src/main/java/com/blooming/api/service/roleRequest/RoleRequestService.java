package com.blooming.api.service.roleRequest;

import com.blooming.api.entity.RoleRequest;
import com.blooming.api.repository.roleRequest.IRoleRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleRequestService {

    private final IRoleRequestRepository roleRequestRepository;

    public RoleRequestService(IRoleRequestRepository roleRequestRepository) {
        this.roleRequestRepository = roleRequestRepository;
    }

    @Transactional
    public ResponseEntity<?> addRoleRequest(RoleRequest roleRequest) {
        RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);
        return ResponseEntity.ok(savedRoleRequest);
    }

    @Transactional
    public ResponseEntity<?> updateRoleRequest(Long id, RoleRequest roleRequest) {
        Optional<RoleRequest> existingRoleRequestOpt = roleRequestRepository.findById(id);

        if(existingRoleRequestOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Role request not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        RoleRequest existingRoleRequest = existingRoleRequestOpt.get();

        existingRoleRequest.setRequestStatus(roleRequest.isRequestStatus());

        RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);
        return ResponseEntity.ok(savedRoleRequest);
    }

    @Transactional
    public ResponseEntity<?> deleteRoleRequest(Long id) {
        Optional<RoleRequest> existingRoleRequestOpt = roleRequestRepository.findById(id);

        if(existingRoleRequestOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Role request not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        roleRequestRepository.deleteById(id);
        return ResponseEntity.ok("Role request deleted successfully");
    }
}
