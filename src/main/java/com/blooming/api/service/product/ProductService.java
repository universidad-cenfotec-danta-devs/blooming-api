package com.blooming.api.service.product;

import com.blooming.api.entity.Product;
import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.request.ProductUpdateRequest;
import com.blooming.api.response.dto.ProductDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public ResponseEntity<?> getAllProductsFromNursery(Long idNursery, int page, int size, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();

        List<ProductDTO> productDTOS = new ArrayList<>();
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> products = productRepository.findAllByNurseryId(idNursery, pageable);
        for (Product product : products.getContent()) {
            ProductDTO productDTO = ParsingUtils.toProductDTO(product);
            productDTOS.add(productDTO);
        }
        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI());
        meta.setTotalPages(products.getTotalPages());
        meta.setTotalElements(products.getTotalElements());
        meta.setPageNumber(products.getNumber() + 1);
        meta.setPageSize(products.getSize());

        if (!products.isEmpty()){
            data.put("userEmail", products.getContent().get(0).getNursery().getNurseryAdmin().getEmail());
        }

        data.put("products", productDTOS);
        return new GlobalHandlerResponse().handleResponse("Products retrieve succesfully", data, HttpStatus.OK, meta);
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