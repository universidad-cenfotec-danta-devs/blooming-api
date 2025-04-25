package com.blooming.api.repository;

import com.blooming.api.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findAllByUserId(Long userId, Pageable pageable);
}
