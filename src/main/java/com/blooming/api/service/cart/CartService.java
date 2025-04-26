package com.blooming.api.service.cart;

import com.blooming.api.entity.Cart;
import com.blooming.api.entity.User;
import com.blooming.api.repository.cart.ICartRepository;
import com.blooming.api.repository.user.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService implements ICartService {

    private final IUserRepository userRepository;
    private final ICartRepository cartRepository;

    public CartService(IUserRepository userRepository, ICartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public ResponseEntity<Cart> createUserCart(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Cart newCart = new Cart();
        newCart.setUser(user.get());

        Cart savedCart = cartRepository.save(newCart);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCart);
    }

    @Override
    public ResponseEntity<Cart> getUserCart(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Cart cart = cartRepository.findById(userId).get();
        if (cart.getCartItems().isEmpty()) {
            cart.setCartItems(new ArrayList<>());
        }
        return ResponseEntity.ok(cart);
    }
}
