package com.blooming.api.service.product;

import com.blooming.api.entity.Product;
import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.request.ProductUpdateRequest;
import com.blooming.api.response.dto.ProductDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product with id " + id + "not found"));
    }

    @Override
    public Page<ProductRequest> getAllProductsFromNursery(Long idNursery, int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return productRepository.findAllByNurseryId(idNursery, pageable);
    }

    @Override
    public ResponseEntity<?> removeProductFromNursery(Long idProduct, HttpServletRequest request) {
        productRepository.deleteById(idProduct);
        return ResponseEntity.ok(new GlobalHandlerResponse().handleResponse("Product deleted", HttpStatus.OK, request));
    }

    @Override
    public ProductDTO updateProductById(Long productId, ProductUpdateRequest request) {
        Product currentProduct = getProductById(productId);

        if (request.name() != null) {
            currentProduct.setName(request.name());
        }
        if(request.description() != null) {
            currentProduct.setDescription(request.description());
        }
        if (request.price() != currentProduct.getPrice()) {
            currentProduct.setPrice(request.price());
        }

        Product updatedProduct = productRepository.save(currentProduct);
        return ParsingUtils.toProductDTO(updatedProduct);
    }
}