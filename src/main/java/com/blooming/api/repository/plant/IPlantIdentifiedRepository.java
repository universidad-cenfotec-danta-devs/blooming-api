package com.blooming.api.repository.plant;

import com.blooming.api.entity.PlantIdentified;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPlantIdentifiedRepository extends JpaRepository<PlantIdentified, Long> {
    Page<PlantIdentified> findByUserId(Long userId, Pageable pageable);
}
