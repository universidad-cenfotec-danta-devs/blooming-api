package com.blooming.api.service.plant;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.response.dto.PlantIdentifiedDTO;
import org.springframework.data.domain.Page;

public interface IPlantIdentifiedService {
    PlantIdentifiedDTO register(PlantIdentified plantIdentified);

    PlantIdentified getById(Long id);

    PlantIdentifiedDTO getByIdDTO(Long id);

    Page<PlantIdentifiedDTO> getAllPlants(int page, int size);

    Page<PlantIdentifiedDTO> getAllPlantsByUser(long userId, int page, int size);
}
