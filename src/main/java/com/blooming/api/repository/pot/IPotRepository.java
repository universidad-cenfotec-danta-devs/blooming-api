package com.blooming.api.repository.pot;

import com.blooming.api.entity.Pot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPotRepository extends JpaRepository<Pot, Long> {
    Page<Pot> findByStatus(boolean status, Pageable pageable);
}
