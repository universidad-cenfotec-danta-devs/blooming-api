package com.blooming.api.service.watering;

import com.blooming.api.entity.User;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.response.dto.WateringDayDTO;

import java.util.List;

public interface IWateringPlanService {
    WateringPlan register(List<WateringDayDTO> wateringDays, User user);
    WateringPlan getWateringPlanById(Long id);
}
