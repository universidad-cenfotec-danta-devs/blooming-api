package com.blooming.api.service.role;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IRoleService {
    ResponseEntity<?> register(Role role);

    Optional<Role> findByName(RoleEnum name);
}
