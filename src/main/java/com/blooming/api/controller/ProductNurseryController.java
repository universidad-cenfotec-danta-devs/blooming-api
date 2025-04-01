package com.blooming.api.controller;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.productNursery.IProductNurseryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("api/products-nursery")
public class ProductNurseryController {

    @Autowired
    private IProductNurseryService productNurseryService;

    @PostMapping("/add-product/{idNursery}/{idProduct}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProductToNursery(@PathVariable Long idNursery,@PathVariable Long idProduct, HttpServletRequest request) {

        return productNurseryService.addProductToNursery(idNursery, idProduct, request);
    }
}

//ToDo Metodo para tabla intermedia
//    @Override
//    public ResponseEntity<?> addProductToNursery(Long idNursery,Product product, HttpServletRequest request) {
//        Optional<Nursery> currentNursery = nurseryRepository.findById(idNursery);
//        if (currentNursery.isPresent()){
//            Product tosaveProdut = new Product();
//            if (product.getId() != null){
//                Optional<Product> foundProduct = productRepository.findById(product.getId());
//                if (foundProduct.isPresent()){
//                    tosaveProdut = foundProduct.get();
//                }
//            }
//            if (currentNursery.get().getProductNurseryList().contains(tosaveProdut)){
//                return new GlobalHandlerResponse().handleResponse("Product is already on the list", HttpStatus.BAD_REQUEST, request);
//            }
//            else {
//                currentNursery.get().getProductNurseryList().add(tosaveProdut);
//            }
//        }
//        Optional<Product> foundProduct = productRepository.findById(id);
//        return null;
//    }
