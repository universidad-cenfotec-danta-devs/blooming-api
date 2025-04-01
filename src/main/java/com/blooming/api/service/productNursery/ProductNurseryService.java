package com.blooming.api.service.productNursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.repository.product.IProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class ProductNurseryService implements IProductNurseryService{

    @Autowired
    private INurseryRepository nurseryRepository;

    @Autowired
    private IProductRepository productRepository;

    @Override
    public ResponseEntity<?> addProductToNursery(Long idNursery, Long idProduct, HttpServletRequest request) {
        Optional<Nursery> currentNursery = nurseryRepository.findById(idNursery);
        Optional<Product> foundProduct = productRepository.findById(idProduct);
        if (currentNursery.isPresent()){
            Product toSaveProduct = new Product();
            if (foundProduct.isPresent()){

            }
        }
        return null;
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