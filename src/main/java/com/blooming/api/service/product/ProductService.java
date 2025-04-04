package com.blooming.api.service.product;

import com.blooming.api.repository.product.IProductRepository;
import com.blooming.api.request.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<ProductRequest> getAllProductsFromNursery(Long idNursery, int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return productRepository.findAllByNurseryId(idNursery, pageable);
    }
}
