package com.blooming.api.service.watering;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.entity.WateringDay;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.repository.watering.IWateringDayRepository;
import com.blooming.api.repository.watering.IWateringPlanRepository;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.WateringPlanDTO;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WateringPlanService implements IWateringPlanService {

    private final IWateringPlanRepository wateringPlanRepository;
    private final IWateringDayRepository wateringDayRepository;

    public WateringPlanService(IWateringPlanRepository wateringPlanRepository, IWateringDayRepository wateringDayRepository) {
        this.wateringPlanRepository = wateringPlanRepository;
        this.wateringDayRepository = wateringDayRepository;
    }

    @Transactional
    @Override
    public WateringPlan register(List<WateringDayDTO> wateringDays, PlantIdentified plantIdentified) {
        List<WateringDay> list = new ArrayList<>();

        WateringPlan wateringPlan = new WateringPlan(list, plantIdentified);
        wateringPlanRepository.save(wateringPlan);
        for (WateringDayDTO wateringDayDTO : wateringDays) {
            WateringDay wateringDay = new WateringDay(
                    wateringDayDTO.getDay(),
                    wateringDayDTO.getMonth(),
                    wateringDayDTO.getYear(),
                    wateringDayDTO.getRecommendation()
            );
            wateringDay.setWateringPlan(wateringPlan);
            wateringDay = wateringDayRepository.save(wateringDay);
            list.add(wateringDay);
        }
        wateringPlan.setWateringDays(list);
        return wateringPlanRepository.save(wateringPlan);
    }

    @Override
    public Page<WateringPlanDTO> getWateringPlansByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WateringPlan> wateringPlansPage = wateringPlanRepository.findByPlantUserId(userId, pageable);
        return wateringPlansPage.map(ParsingUtils::toWateringPlanDTO);
    }

    @Override
    public WateringPlan getWateringPlanById(Long id) {
        return wateringPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Watering plan not found with id: " + id));
    }

    @Override
    public WateringDay addImageToWateringDay(Long wateringDayId, String imageUrl) {
        WateringDay wateringDay = wateringDayRepository.findById(wateringDayId)
                .orElseThrow(() -> new EntityNotFoundException("Watering Day not found with id: " + wateringDayId));
        wateringDay.setImageURL(imageUrl);
        return wateringDayRepository.save(wateringDay);
    }


    @Override
    @Transactional
    public boolean activateWateringPlans(PlantIdentified plantIdentified) {
        try {
            int updatedRows = wateringPlanRepository.activateByPlantId(plantIdentified);
            return updatedRows > 0;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public boolean deactivateWateringPlans(PlantIdentified plantIdentified) {
        try {
            int updatedRows = wateringPlanRepository.deactivateByPlantId(plantIdentified);
            return updatedRows > 0;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
