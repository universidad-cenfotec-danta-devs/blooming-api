package com.blooming.api.controller;

import com.blooming.api.entity.Product;
import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.product.IProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request
    ){
        return productService.getAllProducts(page, size, request);
    }

    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductsByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request)
    {
        return productService.getProductsByName(name, page, size, request);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createProduct(@RequestBody Product product, HttpServletRequest request){
        return productService.createProduct(product, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request){
        return productService.deleteProduct(id, request);
    }
}
