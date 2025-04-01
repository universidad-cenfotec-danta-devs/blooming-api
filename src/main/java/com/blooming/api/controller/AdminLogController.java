package com.blooming.api.controller;

import com.blooming.api.entity.AdminLog;
import com.blooming.api.entity.User;
import com.blooming.api.repository.logs.IAdminLogsRepository;
import com.blooming.api.response.http.GlobalResponseHandler;
import com.blooming.api.response.http.MetaResponse;
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
@RequestMapping("/logs")
public class AdminLogController {

    @Autowired
    private IAdminLogsRepository adminLogsRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getAllLogsPaginated(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<AdminLog> adminLogsPage = adminLogsRepository.findAll(pageable);

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(adminLogsPage.getTotalPages());
        meta.setTotalElements(adminLogsPage.getTotalElements());
        meta.setPageNumber(adminLogsPage.getNumber() + 1);
        meta.setPageSize(adminLogsPage.getSize());

        return new GlobalResponseHandler()._handleResponse("User retrieved successfully", adminLogsPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<AdminLog> createAdminLog(@RequestBody AdminLog adminLog) {
        AdminLog savedLog = adminLogsRepository.save(adminLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLog);
    }
}
