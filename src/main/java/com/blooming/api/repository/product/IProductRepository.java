package com.blooming.api.repository.product;

import com.blooming.api.entity.Product;
import com.blooming.api.entity.ProductNursery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface IProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByName(String name);
}
