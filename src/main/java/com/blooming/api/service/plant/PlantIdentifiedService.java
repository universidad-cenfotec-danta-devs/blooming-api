package com.blooming.api.service.plant;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.repository.plant.IPlantIdentifiedRepository;
import com.blooming.api.response.dto.PlantIdentifiedDTO;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlantIdentifiedService implements IPlantIdentifiedService {

    private final IPlantIdentifiedRepository plantIdentifiedRepository;

    public PlantIdentifiedService(IPlantIdentifiedRepository plantIdentifiedRepository) {
        this.plantIdentifiedRepository = plantIdentifiedRepository;
    }

    @Transactional
    @Override
    public PlantIdentifiedDTO register(PlantIdentified plantIdentified) {
        try {
            PlantIdentified save = plantIdentifiedRepository.save(plantIdentified);
            return ParsingUtils.toPlantIdentifiedDTO(save);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlantIdentified getById(Long id) {
        return plantIdentifiedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found with id: " + id));
    }

    @Override
    public PlantIdentifiedDTO getByIdDTO(Long id) {
        return ParsingUtils.toPlantIdentifiedDTO(getById(id));
    }

    @Override
    public Page<PlantIdentifiedDTO> getAllPlants(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be 0 or greater.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1.");
        }

        Page<com.blooming.api.entity.PlantIdentified> plantPage = plantIdentifiedRepository.findAll(PageRequest.of(page, size));
        if (plantPage.isEmpty()) {
            throw new EntityNotFoundException("No plants found on page " + page + " with size " + size);
        }
        return plantPage.map(ParsingUtils::toPlantIdentifiedDTO);
    }

    @Override
    public Page<PlantIdentifiedDTO> getAllPlantsByUser(long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlantIdentified> plantsPage = plantIdentifiedRepository.findByUserId(userId, pageable);
        return plantsPage.map(ParsingUtils::toPlantIdentifiedDTO);
    }

    @Override
    public Page<PlantIdentifiedDTO> getAllActivePlantsByUser(long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlantIdentified> plantsPage = plantIdentifiedRepository.findAllActiveByUserId(userId, pageable);
        return plantsPage.map(ParsingUtils::toPlantIdentifiedDTO);
    }

    @Override
    @Transactional
    public boolean activate(Long plantId) {
        if (!plantIdentifiedRepository.existsById(plantId)) {
            throw new EntityNotFoundException("Plant not found with id: " + plantId);
        }
        int updatedRows = plantIdentifiedRepository.activate(plantId);
        return updatedRows > 0;
    }

    @Override
    @Transactional
    public boolean deactivate(Long plantId) {
        if (!plantIdentifiedRepository.existsById(plantId)) {
            throw new EntityNotFoundException("Plant not found with id: " + plantId);
        }
        int updatedRows = plantIdentifiedRepository.deactivate(plantId);
        return updatedRows > 0;
    }
}
