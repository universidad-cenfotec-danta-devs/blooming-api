package com.blooming.api.controller;

import com.amazonaws.services.kms.model.NotFoundException;
import com.blooming.api.entity.EntityStatus;
import com.blooming.api.entity.User;
import com.blooming.api.request.NurseryRequest;
import com.blooming.api.request.NurseryUpdateRequest;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.nursery.INurseryService;
import com.blooming.api.service.s3.IS3Service;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/nurseries")
public class NurseryController {

    private final INurseryService nurseryService;
    private final IUserService userService;
    private final IS3Service s3Service;
    private final JwtService jwtService;

    public NurseryController(INurseryService nurseryService, IUserService userService, IS3Service s3Service, JwtService jwtService) {
        this.nurseryService = nurseryService;
        this.userService = userService;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_ADMIN')")
    public ResponseEntity<?> createNursery(@Valid @RequestBody NurseryRequest nurseryRequest,
                                           @RequestParam("img") MultipartFile img,
                                           HttpServletRequest request) {
        try {
            String imgUrl = s3Service.uploadFile("nurseries", img);
            NurseryDTO nursery = nurseryService.createNursery(nurseryRequest, imgUrl);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nursery,
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.name(),
                    "Error processing request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

    @PostMapping("addProductByNurseryAdmin")
    @PreAuthorize("hasRole('NURSERY_ADMIN')")
    public ResponseEntity<?> addProductToNurseryByNurseryAdmin(
            @Valid @RequestBody ProductRequest productRequest,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

            NurseryDTO nurseryDTO = nurseryService.addProductToNursery(user.getId(), productRequest);

            return ResponseEntity.ok(new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nurseryDTO,
                    HttpStatus.OK, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalHandlerResponse().handleResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.name(),
                            "Error processing request: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR, request));
        }
    }

    @GetMapping("/actives")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getAllActiveNurseries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        try {
            Page<NurseryDTO> nurseries = nurseryService.getAllNurseries(page, size, true);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nurseries,
                    HttpStatus.OK, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/non-actives")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getAllNonActiveNurseries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Page<NurseryDTO> nurseries = nurseryService.getAllNurseries(page, size, false);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nurseries,
                    HttpStatus.OK, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getNurseryById(@PathVariable Long id,
                                            HttpServletRequest request) {
        try {
            NurseryDTO nursery = nurseryService.getNurseryDTOById(id);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nursery,
                    HttpStatus.OK, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_ADMIN')")
    public ResponseEntity<?> updateNursery(@PathVariable Long id,
                                           @Valid @RequestBody NurseryUpdateRequest nurseryRequest,
                                           HttpServletRequest request) {
        try {
            NurseryDTO updatedNursery = nurseryService.updateNursery(id, nurseryRequest);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    updatedNursery,
                    HttpStatus.OK, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasRole('ADMIN_USER')")
    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activateNursery(@PathVariable("id") Long nurseryId, HttpServletRequest request) {
        return changeNurseryStatus(nurseryId, EntityStatus.ACTIVATE, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateNursery(@PathVariable("id") Long nurseryId, HttpServletRequest request) {
        return changeNurseryStatus(nurseryId, EntityStatus.DEACTIVATE, request);
    }

    private ResponseEntity<?> changeNurseryStatus(Long nurseryId, EntityStatus status, HttpServletRequest request) {
        try {
            return switch (status) {
                case ACTIVATE -> {
                    nurseryService.activate(nurseryId);
                    yield new GlobalHandlerResponse().handleResponse(
                            HttpStatus.OK.name(),
                            "Nursery activated successfully",
                            HttpStatus.OK, request);
                }
                case DEACTIVATE -> {
                    nurseryService.deactivate(nurseryId);
                    yield new GlobalHandlerResponse().handleResponse(
                            HttpStatus.OK.name(),
                            "Nursery deactivated successfully",
                            HttpStatus.OK, request);
                }
            };
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }

    }
}