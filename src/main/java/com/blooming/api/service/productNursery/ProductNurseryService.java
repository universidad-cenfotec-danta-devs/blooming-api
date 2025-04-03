package com.blooming.api.service.productNursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.entity.ProductNursery;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.repository.productNursery.IProductNurseryRepository;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class ProductNurseryService implements IProductNurseryService{

    @Autowired
    private INurseryRepository nurseryRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IProductNurseryRepository productNurseryRepository;

    @Override
    public ResponseEntity<?> getProductsByNursery(Long idNursery, int page, int size, HttpServletRequest request) {
        List<Product> productsList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page-1, size);
        Optional<Nursery> currentNursery = nurseryRepository.findById(idNursery);
        if (currentNursery.isPresent()){
            for (ProductNursery productNursery: currentNursery.get().getProductNurseryList()){
                productsList.add(productNursery.getProduct());
            }
            Page<Nursery> nurseryPage = nurseryRepository.findAll(pageable);
            MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI().toString());
            meta.setTotalPages(nurseryPage.getTotalPages());
            meta.setTotalElements(nurseryPage.getTotalElements());
            meta.setPageNumber(nurseryPage.getNumber());
            meta.setPageSize(nurseryPage.getSize());

            return new GlobalHandlerResponse().handleResponse("Products from nursery "+ currentNursery.get().getName() +" retrieved successfully", productsList, HttpStatus.OK, meta, request);
        }

        return new GlobalHandlerResponse().handleResponse("Product or nursery not found or already removed", HttpStatus.NOT_FOUND, request);
    }

    @Override
    public ResponseEntity<?> addProductToNursery(Long idNursery, Long idProduct, HttpServletRequest request) {
        Optional<Nursery> currentNursery = nurseryRepository.findById(idNursery);
        if (currentNursery.isPresent()){
            Optional<Product> currentProduct = productRepository.findById(idProduct);
            ProductNursery toSaveProductNursery = new ProductNursery();
            if (currentProduct.isPresent()){
                toSaveProductNursery.setNursery(currentNursery.get());
                toSaveProductNursery.setProduct(currentProduct.get());
                productNurseryRepository.save(toSaveProductNursery);

                return new GlobalHandlerResponse().handleResponse("Product added successfully", toSaveProductNursery, HttpStatus.CREATED, request);
            }
        }
        return new  GlobalHandlerResponse().handleResponse("Product or nursery not found", HttpStatus.NOT_FOUND, request);
    }

    @Override
    public ResponseEntity<?> removeProductFromNursery(Long idNursery, Long idProduct, HttpServletRequest request) {
        Optional<Nursery> currentNursery = nurseryRepository.findById(idNursery);
        if (currentNursery.isPresent()){
            List<ProductNursery> currentNurseryProducts = currentNursery.get().getProductNurseryList();
            ProductNursery toRemoveNurseryProduct = null;

            for (ProductNursery productNursery: currentNurseryProducts){
                if (productNursery.getProduct().getId() == idProduct){
                    toRemoveNurseryProduct = productNursery;
                    break;
                }
            }

            if (toRemoveNurseryProduct != null){
                productNurseryRepository.delete(toRemoveNurseryProduct);
                return new GlobalHandlerResponse().handleResponse(toRemoveNurseryProduct.getProduct().getName()+" removed from nursery", HttpStatus.OK, request);
            }
        }
        return new GlobalHandlerResponse().handleResponse("Product or nursery not found", HttpStatus.NOT_FOUND, request);
    }
}