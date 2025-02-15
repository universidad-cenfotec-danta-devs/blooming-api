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
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(IRoleService roleService, IUserService userService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

        if (roleService.findByName(RoleEnum.ADMIN_USER).isEmpty()) {
            Role superAdminRole = new Role();
            superAdminRole.setName(RoleEnum.ADMIN_USER);
            superAdminRole.setDescription(RoleEnum.ADMIN_USER.name());
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
        user1.setUsername("user1");
        user1.setEmail(USER_EMAIL);
        user1.setPassword(passwordEncoder.encode("user123"));
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
        superAdmin.setUsername("admin_user");
        superAdmin.setEmail(ADMIN_USER);
        superAdmin.setPassword(passwordEncoder.encode("admin"));
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
        designerUser.setUsername("designer_user");
        designerUser.setEmail(DESIGNER_USER);
        designerUser.setPassword(passwordEncoder.encode("designer123"));
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
        nurseryUser.setUsername("nursery_user");
        nurseryUser.setEmail(NURSERY_USER);
        nurseryUser.setPassword(passwordEncoder.encode("nursery123"));
        nurseryUser.setRole(optionalRole.get());
        userService.register(nurseryUser, RoleEnum.NURSERY_USER);

    }
}