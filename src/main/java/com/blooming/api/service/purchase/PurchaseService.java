package com.blooming.api.service.purchase;

import com.amazonaws.services.kms.model.NotFoundException;
import com.blooming.api.entity.CartItem;
import com.blooming.api.entity.Purchase;
import com.blooming.api.entity.User;
import com.blooming.api.repository.IPurchaseRepository;
import com.blooming.api.repository.cartItem.ICartItemRepository;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.UserService;
import com.blooming.api.utils.PaginationUtils;
import com.blooming.api.utils.ParsingUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService implements IPurchaseService {

    private final IPurchaseRepository purchaseRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final ICartItemRepository cartItemRepository;

    public PurchaseService(IPurchaseRepository purchaseRepository, JwtService jwtService, UserService userService, ICartItemRepository cartItemRepository) {
        this.purchaseRepository = purchaseRepository;
        this.jwtService = jwtService;
        this.userService = userService;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public ResponseEntity<?> getAllPurchases(int page, int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Purchase> purchases = purchaseRepository.findAll(pageable);
        return PaginationUtils.getPaginatedResponse(purchases.map(ParsingUtils::toPurchaseDTO), request);
    }

    @Override
    public ResponseEntity<?> getAllPurchasesByUser(int page, int size, HttpServletRequest request) {
        User currentUser;
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String userEmail = jwtService.extractUsername(token);
        currentUser = userService.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Purchase> purchases = purchaseRepository.findAllByUserId(currentUser.getId(), pageable);

        return PaginationUtils.getPaginatedResponse(purchases.map(ParsingUtils::toPurchaseDTO), request);
    }

    @Override
    public ResponseEntity<?> createPurchase(Purchase purchase, HttpServletRequest request) {
        List<CartItem> items = cartItemRepository.findAllById(
                purchase.getCartItems().stream().map(CartItem::getId).toList()
        );
        items.forEach(cartItem -> cartItem.setPurchase(purchase));
        purchase.setCartItems(items);
        Purchase purchaseCreated = purchaseRepository.save(purchase);
        return new GlobalHandlerResponse().handleResponse(HttpStatus.CREATED.name(), ParsingUtils.toPurchaseDTO(purchaseCreated), HttpStatus.CREATED, request);
    }

    @Override
    public ResponseEntity<?> getPurchaseById(Long idPurchase, HttpServletRequest request) {
        return new GlobalHandlerResponse().handleResponse(HttpStatus.OK.name(), ParsingUtils.toPurchaseDTO(purchaseRepository.findById(idPurchase).orElseThrow(()->new NotFoundException("Purchase Not found"))), HttpStatus.OK, request);
    }
}
