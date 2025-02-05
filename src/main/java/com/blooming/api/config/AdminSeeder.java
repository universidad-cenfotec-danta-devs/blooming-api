package com.blooming.api.config;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.exception.GlobalExceptionHandler;
import com.blooming.api.service.role.IRoleService;
import com.blooming.api.service.user.IUserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class AdminSeeder {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    private final IRoleService roleService;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(IRoleService roleService, IUserService userService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        this.createRolesIfNotExist();
        this.createDefaultUsers();
    }

    private void createRolesIfNotExist() {
        if (roleService.findByName(RoleEnum.ADMIN_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(RoleEnum.ADMIN_USER);
            userRole.setDescription(RoleEnum.ADMIN_USER.name());
            roleService.register(userRole);
        }
    }

    private void createDefaultUsers() {
        createAdminUser();
    }

    private void createAdminUser() {
        Optional<Role> optionalRole = roleService.findByName(RoleEnum.ADMIN_USER);
        String SUPER_ADMIN_EMAIL = "admin_user@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(SUPER_ADMIN_EMAIL);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }
        User superAdmin = new User();
        superAdmin.setUsername("admin_user");
        superAdmin.setEmail(SUPER_ADMIN_EMAIL);
        superAdmin.setPassword(passwordEncoder.encode("admin123"));
        superAdmin.setRole(optionalRole.get());
        ResponseEntity<?> register = userService.register(superAdmin);
        if (!register.getStatusCode().is2xxSuccessful()) {
            logger.error(Objects.requireNonNull(register.getBody()).toString());
        }

    }

}