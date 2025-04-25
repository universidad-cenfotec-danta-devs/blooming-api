package com.blooming.api.controller;

import com.blooming.api.entity.Purchase;
import com.blooming.api.service.purchase.IPurchaseService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    private final IPurchaseService purchaseService;

    public PurchaseController(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER', 'DESIGNER_USER')")
    public ResponseEntity<?> getAllPurchases(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        return purchaseService.getAllPurchases(page, size, request);
    }

    @GetMapping("my-purchases")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER', 'DESIGNER_USER')")
    public ResponseEntity<?> getMyPurchases(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                            HttpServletRequest request) {
        return purchaseService.getAllPurchasesByUser(page, size, request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER', 'DESIGNER_USER')")
    public ResponseEntity<?> createPurchase(@RequestBody Purchase purchase, HttpServletRequest request) {
        return purchaseService.createPurchase(purchase, request);
    }

    @GetMapping("{idPurchase}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'NURSERY_USER', 'DESIGNER_USER')")
    public ResponseEntity<?> getPurchaseById(@PathVariable Long idPurchase, HttpServletRequest request) {
        return purchaseService.getPurchaseById(idPurchase, request);
    }
}
