package com.blooming.api.service.watering;

import com.blooming.api.entity.User;
import com.blooming.api.entity.WateringDay;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.repository.watering.IWateringPlanRepository;
import com.blooming.api.response.dto.WateringDayDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WateringPlanService implements IWateringPlanService {

    private final IWateringPlanRepository wateringPlanRepository;

    public WateringPlanService(IWateringPlanRepository wateringPlanRepository) {
        this.wateringPlanRepository = wateringPlanRepository;
    }

    @Override
    public WateringPlan register(List<WateringDayDTO> wateringDays, User user) {
        List<WateringDay> list = new ArrayList<>();
        WateringPlan wateringPlan = new WateringPlan();
        wateringPlan.setUser(user);
        for (WateringDayDTO wateringDayDTO : wateringDays) {
            WateringDay wateringDay = new WateringDay(
                    wateringDayDTO.getDay(),
                    wateringDayDTO.getMonth(),
                    wateringDayDTO.getYear(),
                    wateringDayDTO.getRecommendation()
            );
            wateringDay.setWateringPlan(wateringPlan);
            list.add(wateringDay);
        }

        wateringPlan.setWateringDays(list);
        return wateringPlanRepository.save(wateringPlan);
    }

    @Override
    public WateringPlan getWateringPlanById(Long id) {
        return wateringPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Watering plan not found with id: " + id));
    }
}
