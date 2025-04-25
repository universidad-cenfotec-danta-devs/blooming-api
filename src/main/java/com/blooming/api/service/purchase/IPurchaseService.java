package com.blooming.api.service.purchase;

import com.blooming.api.entity.Purchase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IPurchaseService {
    ResponseEntity<?> getAllPurchases(int page, int size, HttpServletRequest request);

    ResponseEntity<?> getAllPurchasesByUser(int page, int size, HttpServletRequest request);

    ResponseEntity<?> createPurchase(Purchase purchase, HttpServletRequest request);

    ResponseEntity<?> getPurchaseById(Long idPurchase, HttpServletRequest request);
}
