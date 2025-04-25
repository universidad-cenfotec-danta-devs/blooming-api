package com.blooming.api.service.cart;

import com.blooming.api.entity.Cart;
import org.springframework.http.ResponseEntity;

public interface ICartService {
    ResponseEntity<Cart> createUserCart(Long userId);
    ResponseEntity<Cart> getUserCart(Long userId);
}
