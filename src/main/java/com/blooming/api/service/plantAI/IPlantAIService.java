package com.blooming.api.service.plantAI;

import com.blooming.api.response.dto.PlantDetailsDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;

import java.util.List;

public interface IPlantAIService {
    List<PlantSuggestionDTO> identifyImage(byte[] bytes);
    PlantDetailsDTO getPlantInformationByName(String plantName);
}
