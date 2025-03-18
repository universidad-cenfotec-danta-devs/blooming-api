package com.blooming.api.service.user;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.repository.role.IRoleRepository;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    IRoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("plainPassword");

        testRole = new Role();
        testRole.setName(RoleEnum.SIMPLE_USER);
    }

    @Test
    void registerSuccess() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleEnum.SIMPLE_USER)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<?> response = userService.register(testUser, RoleEnum.SIMPLE_USER);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testUser, response.getBody());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerFailsWhenEmailExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = userService.register(testUser, RoleEnum.SIMPLE_USER);

        assertEquals(409, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Email already in use", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void registerFailsWhenRoleNotFound() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleEnum.SIMPLE_USER)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.register(testUser, RoleEnum.SIMPLE_USER);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Role not found", ((Map<?, ?>) response.getBody()).get("message"));
    }
}