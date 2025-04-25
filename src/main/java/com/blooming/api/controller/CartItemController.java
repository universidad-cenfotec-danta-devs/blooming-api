package com.blooming.api.controller;

import com.blooming.api.entity.Cart;
import com.blooming.api.entity.CartItem;
import com.blooming.api.repository.cart.ICartRepository;
import com.blooming.api.repository.cartItem.ICartItemRepository;
import com.blooming.api.response.dto.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cartItem")
public class CartItemController {

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private ICartRepository cartRepository;

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItem>> getAllCartItemsByCartId(@PathVariable Long cartId) {
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cartId);
        if (cartItems.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cartItems);
    }

    @PostMapping
    public ResponseEntity<CartItem> createCartItem(@RequestBody CartItemDTO cartItemDTO) {
        Optional<Cart> cartOptional = cartRepository.findById(cartItemDTO.getCartId());
        if (cartOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CartItem cartItem = new CartItem();
        cartItem.setCart(cartOptional.get());
        cartItem.setItemName(cartItemDTO.getItemName());
        cartItem.setItemType(cartItemDTO.getItemType());
        cartItem.setPrice(cartItemDTO.getPrice());
        cartItem.setQuantity(cartItemDTO.getQuantity());

        CartItem savedItem = cartItemRepository.save(cartItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @DeleteMapping("/soft/{id}")
    public ResponseEntity<Void> softDeleteCartItem(@PathVariable Long id) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(id);
        if (cartItemOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CartItem cartItem = cartItemOptional.get();
        cartItem.setActive(false);
        cartItemRepository.save(cartItem);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{id}")
    public ResponseEntity<Void> hardDeleteCartItem(@PathVariable Long id) {
        if (!cartItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        cartItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<CartItem> patchCartItemQuantity(@PathVariable Long id, @RequestBody Integer itemQuantity) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(id);
        if (cartItemOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CartItem cartItem = cartItemOptional.get();
        cartItem.setQuantity(itemQuantity);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return ResponseEntity.ok(updatedCartItem);
    }
}
