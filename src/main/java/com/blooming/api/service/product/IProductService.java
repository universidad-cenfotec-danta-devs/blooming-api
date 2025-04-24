package com.blooming.api.service.product;

import com.blooming.api.entity.Product;
import com.blooming.api.request.ProductUpdateRequest;
import com.blooming.api.response.dto.ProductDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IProductService {
    Product getProductById(Long id);
    ResponseEntity<?> getAllProductsFromNursery(Long idNursery, int page, int size, HttpServletRequest request);
    ResponseEntity<?> removeProductFromNursery(Long idProduct, HttpServletRequest request);
    ProductDTO updateProductById(Long productId, ProductUpdateRequest productRequest);
}
