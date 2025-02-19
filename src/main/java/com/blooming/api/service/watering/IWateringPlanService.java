package com.blooming.api.service.watering;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.response.dto.WateringDayDTO;

import java.util.List;

public interface IWateringPlanService {
    WateringPlan register(List<WateringDayDTO> wateringDays, PlantIdentified plantIdentified);
    WateringPlan getWateringPlanById(Long id);
}
