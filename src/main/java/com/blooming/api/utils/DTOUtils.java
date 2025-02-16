package com.blooming.api.utils;

import com.blooming.api.response.dto.PlantDetailsDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.query.sqm.ParsingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DTOUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<List<PlantSuggestionDTO>> parsePlantSuggestions(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<PlantSuggestionDTO> suggestionsList = new ArrayList<>();

            JsonNode suggestions = root.path("result").path("classification").path("suggestions");
            for (JsonNode suggestion : suggestions) {
                String name = suggestion.path("name").asText();
                double probability = suggestion.path("probability").asDouble();
                String probabilityPercentage = String.format("%.0f%%", probability * 100);

                JsonNode firstImage = suggestion.path("similar_images").path(0);
                String imageUrl = firstImage.path("url").asText();
                String imageUrlSmall = firstImage.path("url_small").asText();
                double similarity = firstImage.path("similarity").asDouble();
                String similarityPercentage = String.format("%.0f%%", similarity * 100);

                PlantSuggestionDTO dto = new PlantSuggestionDTO();
                dto.setName(name);
                dto.setProbabilityPercentage(probabilityPercentage);
                dto.setSimilarityPercentage(similarityPercentage);
                dto.setImageUrl(imageUrl);
                dto.setImageUrlSmall(imageUrlSmall);

                suggestionsList.add(dto);
            }

            return Optional.of(suggestionsList);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing plant suggestions", e);
        }
    }

    public static Optional<PlantDetailsDTO> parsePlantDetails(String jsonResponse) {
        try {
            JsonNode plantDetailsNode = objectMapper.readTree(jsonResponse);
            String name = plantDetailsNode.path("name").asText();
            String watering = plantDetailsNode.path("watering").asText();
            String bestWatering = plantDetailsNode.path("best_watering").asText();
            String bestLightCondition = plantDetailsNode.path("best_light_condition").asText();
            String bestSoilType = plantDetailsNode.path("best_soil_type").asText();

            PlantDetailsDTO dto = new PlantDetailsDTO();
            dto.setName(name);
            dto.setWatering(watering);
            dto.setBestWatering(bestWatering);
            dto.setBestLightCondition(bestLightCondition);
            dto.setBestSoilType(bestSoilType);

            return Optional.of(dto);

        } catch (JsonParseException e) {
            throw new ParsingException("Error parsing plant details: Invalid JSON format" + e.getMessage());
        } catch (JsonMappingException e) {
            throw new ParsingException("Error mapping plant details: Missing expected fields" + e.getMessage());
        } catch (Exception e) {
            throw new ParsingException("Error parsing plant details: " + e.getMessage());
        }
    }

}
