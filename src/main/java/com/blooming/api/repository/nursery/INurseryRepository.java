package com.blooming.api.repository.nursery;

import com.blooming.api.entity.Nursery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface INurseryRepository extends JpaRepository<Nursery, Long> {
    Page<Nursery> findNurseriesByStatus(boolean status, Pageable pageable);
    Optional<Nursery> findByNurseryAdminId(Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Nursery n SET n.status = true WHERE n.id = :nurseryId")
    int activate(@Param("nurseryId") Long nurseryId);

    @Modifying
    @Transactional
    @Query("UPDATE Nursery n SET n.status = false WHERE n.id = :nurseryId")
    int deactivate(@Param("nurseryId") Long nurseryId);
}
