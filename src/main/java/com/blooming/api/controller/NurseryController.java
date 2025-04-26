package com.blooming.api.controller;

import com.amazonaws.services.kms.model.NotFoundException;
import com.blooming.api.entity.EntityStatus;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.User;
import com.blooming.api.request.NurseryRequest;
import com.blooming.api.request.NurseryUpdateRequest;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.request.ProductUpdateRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.response.dto.ProductDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.nursery.INurseryService;
import com.blooming.api.service.product.IProductService;
import com.blooming.api.service.s3.IS3Service;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.utils.PaginationUtils;
import com.blooming.api.utils.ParsingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/nurseries")
public class NurseryController {

    private final INurseryService nurseryService;
    private final IUserService userService;
    private final IS3Service s3Service;
    private final JwtService jwtService;
    private final IProductService productService;

    public NurseryController(INurseryService nurseryService, IUserService userService, IS3Service s3Service, JwtService jwtService, IProductService productService) {
        this.nurseryService = nurseryService;
        this.userService = userService;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> getAllNurseries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request
    ) {
        return nurseryService.getAllNurseries(page, size, request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> createNursery(@RequestPart("nurseryRequest") @Valid NurseryRequest nurseryRequest,
                                           @RequestPart("nurseryImg") MultipartFile nurseryImg,
                                           HttpServletRequest request) {
        try {
            User nurseryUser;
            if (Objects.equals(nurseryRequest.userEmail(), "")) {
                String token = request.getHeader("Authorization").replace("Bearer ", "");
                String userEmail = jwtService.extractUsername(token);
                nurseryUser = userService.findByEmail(userEmail)
                        .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
            } else {
                nurseryUser = userService.findByEmail(nurseryRequest.userEmail()).orElseThrow(() ->
                        new NotFoundException("User not found with email: " + nurseryRequest.userEmail()));
            }
            String imgUrl = s3Service.uploadFile("nurseries", nurseryImg);
            if (imgUrl == null) {
                throw new IOException("Image not found");
            }
            NurseryDTO nursery = nurseryService.createNursery(nurseryRequest, nurseryUser, imgUrl);
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
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> addProductToNurseryByNurseryAdmin(
            @Valid @RequestBody ProductRequest productRequest,
            HttpServletRequest request) {
        try {
            User nurseryUser;
            if (productRequest.userEmail() == null) {
                String token = request.getHeader("Authorization").replace("Bearer ", "");
                String userEmail = jwtService.extractUsername(token);
                nurseryUser = userService.findByEmail(userEmail)
                        .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
            } else {
                nurseryUser = userService.findByEmail(productRequest.userEmail()).orElseThrow(() ->
                        new NotFoundException("User not found with email: " + productRequest.userEmail()));
            }

            NurseryDTO nurseryDTO = nurseryService.addProductToNursery(nurseryUser.getId(), productRequest);

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

    @PostMapping("add-product/{idNursery}")
    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    public ResponseEntity<?> addProductToNursery(@PathVariable Long idNursery, @RequestBody ProductRequest product) {
        try {
            return ResponseEntity.ok(nurseryService.addProductToNurseryAsAdmin(idNursery, product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing request: " + e.getMessage());
        }

    }

    @DeleteMapping("remove-product/{idProduct}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> removeProductFromNurseryByNurseryAdmin(@PathVariable Long idProduct, HttpServletRequest request) {
        try {
            productService.removeProductFromNursery(idProduct, request);
            return ResponseEntity.ok(new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    "Product removed successfully",
                    HttpStatus.OK, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GlobalHandlerResponse().handleResponse(
                            HttpStatus.NOT_FOUND.name(),
                            "Id not found: " + e.getMessage(),
                            HttpStatus.NOT_FOUND, request));
        }
    }

    @GetMapping("get-products/{idNursery}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getAllProductsByNurseryId(@PathVariable Long idNursery,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       HttpServletRequest request) {
        try {
            return productService.getAllProductsFromNursery(idNursery, page, size, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/actives")
    public ResponseEntity<?> getAllActiveNurseries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        try {
            Page<NurseryDTO> nurseries = nurseryService.getAllNurseries(page, size, true);
            return PaginationUtils.getPaginatedResponse(nurseries, request);
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

    @GetMapping("my-nursery")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getMyNursery(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
            Nursery nursery = nurseryService.getNurseryByNurseryAdminId(user.getId());
            NurseryDTO nurseryDTO = ParsingUtils.toNurseryDTO(nursery);
            return ResponseEntity.ok(new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    nurseryDTO,
                    HttpStatus.OK, request));
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.NOT_FOUND.name(),
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("my-products")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getMyProducts(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
            Nursery nursery = nurseryService.getNurseryByNurseryAdminId(user.getId());
            return productService.getAllProductsFromNursery(nursery.getId(), page, size, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("nearby")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getNearbyNurseries(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam double userLat,
                                                @RequestParam double userLng,
                                                @RequestParam(required = false, defaultValue = "10") double radiusKm, HttpServletRequest request) {
        Page<NurseryDTO> nearbyNurseries = nurseryService.findNearby(page, size, userLat, userLng, radiusKm);
        return PaginationUtils.getPaginatedResponse(nearbyNurseries, request);

    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
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

    @PatchMapping("update-product/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER')")
    public ResponseEntity<?> updateMyProducts(@PathVariable Long id,
                                              @Valid @RequestBody ProductUpdateRequest productUpdateRequest,
                                              HttpServletRequest request) {
        try {
            ProductDTO updatedProduct = productService.updateProductById(id, productUpdateRequest);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    updatedProduct,
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