package com.blooming.api.service.product;

import com.blooming.api.entity.Product;
import com.blooming.api.repository.product.IProductRepository;
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

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService{

    @Autowired
    private IProductRepository productRepository;

    @Override
    public ResponseEntity<?> getAllProducts(int page, int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page-1,size);
        Page<Product> productPage = productRepository.findAll(pageable);
        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber());
        meta.setPageSize(productPage.getSize());

        return new GlobalHandlerResponse().handleResponse("Products retrieved successfully", productPage.getContent(), HttpStatus.OK, meta);
    }



    @Override
    public ResponseEntity<?> createProduct(Product product, HttpServletRequest request) {
        Product createdProduct = productRepository.save(product);
        return new GlobalHandlerResponse().handleResponse("Product successfully saved", createdProduct, HttpStatus.OK, request);
    }

//Cambiar a update el estado
    @Override
    public ResponseEntity<?> deleteProduct(Long id, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if (foundProduct.isPresent()){
            productRepository.deleteById(id);
            return new GlobalHandlerResponse().handleResponse("Product successfully deleted", foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalHandlerResponse().handleResponse("Product id not found", HttpStatus.NOT_FOUND, request);
        }
    }


    @Override
    public ResponseEntity<?> getProductsByName(String name, int page, int size, HttpServletRequest request) {
        List<Product> foundProducts = productRepository.findAllByName(name);
        Pageable pageable = PageRequest.of(page-1,size);
        Page<Product> productPage = productRepository.findAll(pageable);
        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber());
        meta.setPageSize(productPage.getSize());

        return new GlobalHandlerResponse().handleResponse("Products retrieved successfully", foundProducts, HttpStatus.OK, meta);
    }
}

//ToDo averiguar tabla intermedia
//    @Override
//    public ResponseEntity<?> updateProduct(Long id, Product product, HttpServletRequest request) {
//        Optional<Product> foundProduct = productRepository.findById(id);
//        if (foundProduct.isPresent()){
//            Product currentProduct = foundProduct.get();
//            if (product.getName() != null){
//                currentProduct.setName(product.getName());
//            }
//            if (product.getDescription() != null){
//                currentProduct.setDescription(product.getDescription());
//            }
//        }
//        return new GlobalHandlerResponse().handleResponse(" ", HttpStatus.OK, request);
//    }
//
//    @Override
//



//    Get all products by nursery
//      For each
//        List<ProductNursery> list = nurseryRepository.findById(1L).get().getProductNurseryList();
//        for(ProductNursery pnursery : list){
//            productRepository.save(pnursery.getProduct());
//        }