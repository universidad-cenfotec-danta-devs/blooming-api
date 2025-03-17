package com.blooming.api.controller;

import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.GlobalResponseHandler;
import com.blooming.api.response.http.MetaResponse;
import com.blooming.api.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        return userService.register(user, RoleEnum.SIMPLE_USER);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<User> usersPage = userRepository.findAll(pageable);

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("User retrieved successfully", usersPage.getContent(), HttpStatus.OK, meta);
    }
}
