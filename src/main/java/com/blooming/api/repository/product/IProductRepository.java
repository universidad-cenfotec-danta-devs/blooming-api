package com.blooming.api.repository.product;

import com.blooming.api.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

public interface IProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByNurseryId(Long nurseryId, Pageable pageable);
}
