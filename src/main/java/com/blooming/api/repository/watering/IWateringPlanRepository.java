package com.blooming.api.repository.watering;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.entity.WateringPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IWateringPlanRepository extends JpaRepository<WateringPlan, Long> {

    Page<WateringPlan> findByPlantUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE WateringPlan w SET w.isActive = true WHERE w.plant = :plantIdentified")
    int activateByPlantId(PlantIdentified plantIdentified);

    @Modifying
    @Transactional
    @Query("UPDATE WateringPlan w SET w.isActive = false WHERE w.plant = :plantIdentified")
    int deactivateByPlantId(PlantIdentified plantIdentified);

}
