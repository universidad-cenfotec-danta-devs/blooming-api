package com.blooming.api.repository.watering;

import com.blooming.api.entity.WateringPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IWateringPlanRepository extends JpaRepository<WateringPlan, Long> {
}
