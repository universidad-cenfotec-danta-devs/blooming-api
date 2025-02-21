package com.blooming.api.service.watering;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.entity.WateringDay;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.WateringPlanDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IWateringPlanService {
    WateringPlan register(List<WateringDayDTO> wateringDays, PlantIdentified plantIdentified);

    Page<WateringPlanDTO> getWateringPlansByUser(Long userId, int page, int size);

    WateringPlan getWateringPlanById(Long id);

    WateringDay addImageToWateringDay(Long wateringDayId, MultipartFile image);

    boolean activateWateringPlans(PlantIdentified plantIdentified);

    boolean deactivateWateringPlans(PlantIdentified plantIdentified);
}
