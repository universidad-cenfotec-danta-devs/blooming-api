package com.blooming.api.service.productNursery;

import com.blooming.api.entity.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IProductNurseryService {
    //    ResponseEntity<?> getProductsByNursery(Long id, HttpServletRequest request);
    ResponseEntity<?> addProductToNursery(Long idNursery, Long idProduct, HttpServletRequest request);
//    ResponseEntity<?> removeProductFromNursery(Long id, HttpServletRequest request);
}
