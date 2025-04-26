package com.blooming.api.config;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.service.role.IRoleService;
import com.blooming.api.service.user.IUserService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class UserSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final IRoleService roleService;
    private final IUserService userService;

    public UserSeeder(IRoleService roleService, IUserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
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

        if (roleService.findByName(RoleEnum.SIMPLE_USER).isEmpty()) {
            Role superAdminRole = new Role();
            superAdminRole.setName(RoleEnum.SIMPLE_USER);
            superAdminRole.setDescription(RoleEnum.SIMPLE_USER.name());
            roleService.register(superAdminRole);
        }

        if (roleService.findByName(RoleEnum.NURSERY_USER).isEmpty()) {
            Role superAdminRole = new Role();
            superAdminRole.setName(RoleEnum.NURSERY_USER);
            superAdminRole.setDescription(RoleEnum.NURSERY_USER.name());
            roleService.register(superAdminRole);
        }

        if (roleService.findByName(RoleEnum.DESIGNER_USER).isEmpty()) {
            Role superAdminRole = new Role();
            superAdminRole.setName(RoleEnum.DESIGNER_USER);
            superAdminRole.setDescription(RoleEnum.DESIGNER_USER.name());
            roleService.register(superAdminRole);
        }
    }

    private void createDefaultUsers() {
        createAdmin();
        simpleUser();
        unknownUser();
        createDesigner();
    }

    private void simpleUser() {
        Optional<Role> optionalRole = roleService.findByName(RoleEnum.SIMPLE_USER);
        String USER_EMAIL = "user1@gmail.com";
        Optional<User> optionalUser = userService.findByEmail(USER_EMAIL);

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }
        User user1 = new User();
        user1.setEmail(USER_EMAIL);
        user1.setName("Simple User");
        user1.setGender("male");
        user1.setDateOfBirth(new Date());
        user1.setPassword("user123");
        user1.setRole(optionalRole.get());
        userService.register(user1, RoleEnum.SIMPLE_USER);
    }

    private void createAdmin() {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.ADMIN_USER);
        String ADMIN_USER = "admin_user@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(ADMIN_USER);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }
        User superAdmin = new User();
        superAdmin.setEmail(ADMIN_USER);
        superAdmin.setName("Admin User");
        superAdmin.setGender("male");
        superAdmin.setDateOfBirth(new Date());
        superAdmin.setPassword("admin");
        superAdmin.setRole(optionalRole.get());
        userService.register(superAdmin, RoleEnum.ADMIN_USER);
    }


    private void createDesigner() {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.DESIGNER_USER);
        String DESIGNER_USER = "designer1@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(DESIGNER_USER);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }
        User superAdmin = new User();
        superAdmin.setEmail(DESIGNER_USER);
        superAdmin.setName("Designer User");
        superAdmin.setGender("male");
        superAdmin.setDateOfBirth(new Date());
        superAdmin.setPassword("user123");
        superAdmin.setRole(optionalRole.get());
        userService.register(superAdmin, RoleEnum.DESIGNER_USER);
    }

    private void unknownUser() {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.SIMPLE_USER);
        String UNKNOWN_USER = "unknown_user@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(UNKNOWN_USER);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }

        User superAdmin = new User();
        superAdmin.setEmail(UNKNOWN_USER);
        superAdmin.setName("Anonymous User");
        superAdmin.setGender("male");
        superAdmin.setDateOfBirth(new Date());
        superAdmin.setPassword("unknown_user");
        superAdmin.setRole(optionalRole.get());
        superAdmin.setActive(false);
        userService.register(superAdmin, RoleEnum.SIMPLE_USER);
    }

}