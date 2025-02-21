package com.blooming.api.repository.plant;

import com.blooming.api.entity.PlantIdentified;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IPlantIdentifiedRepository extends JpaRepository<PlantIdentified, Long> {
    Page<PlantIdentified> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM PlantIdentified p WHERE p.user.id = :userId AND p.isActive = true")
    Page<PlantIdentified> findAllActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE PlantIdentified p SET p.isActive = true WHERE p.id = :plantId")
    int activate(Long plantId);

    @Modifying
    @Transactional
    @Query("UPDATE PlantIdentified p SET p.isActive = false WHERE p.id = :plantId")
    int deactivate(Long plantId);
}
