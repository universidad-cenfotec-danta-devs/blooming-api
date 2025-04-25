package com.blooming.api.controller;

import com.blooming.api.entity.Cart;
import com.blooming.api.service.cart.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestParam Long userId) {
        return cartService.createUserCart(userId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        return cartService.getUserCart(userId);
    }

}
