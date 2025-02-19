package com.blooming.api.service.plantAI;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;

import java.util.List;

public interface IPlantAIService {

    List<PlantSuggestionDTO> identifyImage(byte[] bytes);

    PlantIdentified getPlantInformationByName(String plantName, String tokenPlant);

    List<String> generateWateringSchedule(String idAccessToken);

    List<WateringDayDTO> generateWateringDays(String idAccessToken, List<String> wateringDates);

    String askPlantId(String idAccessToken, String question);
}
