package com.blooming.api.service.product;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IProductService {
    ResponseEntity<?> getAllProducts(int page, int size, HttpServletRequest
    request);
//    ResponseEntity<?> getProductsByNursery(Long id, HttpServletRequest request);
    ResponseEntity<?> createProduct(Product product, HttpServletRequest request);
    ResponseEntity<?> deleteProduct(Long id, HttpServletRequest request);
//    ResponseEntity<?> updateProduct(Long id, Product product, HttpServletRequest request);
    ResponseEntity<?> getProductsByName(String name, int page, int size, HttpServletRequest request);
}
