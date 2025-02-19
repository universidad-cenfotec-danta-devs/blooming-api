package com.blooming.api.repository.watering;

import com.blooming.api.entity.WateringDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWateringDayRepository extends JpaRepository<WateringDay, Long> {
}
