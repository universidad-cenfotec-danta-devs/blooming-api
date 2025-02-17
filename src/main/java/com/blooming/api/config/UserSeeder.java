package com.blooming.api.config;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.service.role.IRoleService;
import com.blooming.api.service.user.IUserService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final IRoleService roleService;
    private final IUserService userService;

    public UserSeeder(IRoleService roleService, IUserService userService, PasswordEncoder passwordEncoder) {
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
        createDesignerUser();
        createNurseryUser();
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
        superAdmin.setPassword("admin");
        superAdmin.setRole(optionalRole.get());
        userService.register(superAdmin, RoleEnum.ADMIN_USER);

    }

    private void createDesignerUser() {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.DESIGNER_USER);
        String DESIGNER_USER = "designer_user@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(DESIGNER_USER);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }
        User designerUser = new User();
        designerUser.setEmail(DESIGNER_USER);
        designerUser.setPassword("designer123");
        designerUser.setRole(optionalRole.get());
        userService.register(designerUser, RoleEnum.DESIGNER_USER);

    }

    private void createNurseryUser() {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.NURSERY_USER);
        String NURSERY_USER = "nursery_user@gmail.com";
        Optional<User> optionalSuperAdmin = userService.findByEmail(NURSERY_USER);

        if (optionalRole.isEmpty() || optionalSuperAdmin.isPresent()) {
            return;
        }
        User nurseryUser = new User();
        nurseryUser.setEmail(NURSERY_USER);
        nurseryUser.setPassword("nursery123");
        nurseryUser.setRole(optionalRole.get());
        userService.register(nurseryUser, RoleEnum.NURSERY_USER);

    }
}