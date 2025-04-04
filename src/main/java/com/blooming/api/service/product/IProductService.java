package com.blooming.api.service.product;

import com.blooming.api.entity.Product;
import com.blooming.api.request.ProductRequest;
import org.springframework.data.domain.Page;

public interface IProductService {
    Page<ProductRequest> getAllProductsFromNursery(Long idNursery, int page, int size);
}
