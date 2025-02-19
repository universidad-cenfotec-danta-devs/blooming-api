package com.blooming.api.utils;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.exception.ParsingException;
import com.blooming.api.response.dto.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ParsingUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<List<PlantSuggestionDTO>> parsePlantSuggestions(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<PlantSuggestionDTO> suggestionsList = new ArrayList<>();
            String idAccessToken = root.path("access_token").asText();
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
                dto.setIdAccessToken(idAccessToken);
                dto.setProbabilityPercentage(probabilityPercentage);
                dto.setSimilarityPercentage(similarityPercentage);
                dto.setImageUrl(imageUrl);
                dto.setImageUrlSmall(imageUrlSmall);

                suggestionsList.add(dto);
            }
            return Optional.of(suggestionsList);
        } catch (Exception e) {
            throw new ParsingException("Error parsing plant suggestions", e);
        }
    }

    public static Optional<PlantIdentified> parsePlantDetails(String jsonResponse) {
        try {
            JsonNode plantDetailsNode = objectMapper.readTree(jsonResponse);

            String name = plantDetailsNode.path("name").isNull() ? null : plantDetailsNode.path("name").asText();
            String watering = plantDetailsNode.path("watering").isNull() ? null : plantDetailsNode.path("watering").toString();
            String bestWatering = plantDetailsNode.path("best_watering").isNull() ? null : plantDetailsNode.path("best_watering").asText();
            String bestLightCondition = plantDetailsNode.path("best_light_condition").isNull() ? null : plantDetailsNode.path("best_light_condition").asText();
            String bestSoilType = plantDetailsNode.path("best_soil_type").isNull() ? null : plantDetailsNode.path("best_soil_type").asText();

            PlantIdentified dto = new PlantIdentified();
            dto.setName(name);
            dto.setWatering(watering);
            dto.setBestWatering(bestWatering);
            dto.setBestLightCondition(bestLightCondition);
            dto.setBestSoilType(bestSoilType);

            return Optional.of(dto);

        } catch (JsonParseException e) {
            throw new com.blooming.api.exception.ParsingException("Error parsing plant details: Invalid JSON format" + e.getMessage());
        } catch (JsonMappingException e) {
            throw new com.blooming.api.exception.ParsingException("Error mapping plant details: Missing expected fields" + e.getMessage());
        } catch (Exception e) {
            throw new com.blooming.api.exception.ParsingException("Error parsing plant details: " + e.getMessage());
        }
    }

    public static List<WateringDayDTO> parseWateringDays(ResponseEntity<String> response) {
        List<WateringDayDTO> wateringDays = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode messages = root.path("messages");

            for (JsonNode message : messages) {
                String content = message.path("content").asText();
                if (content.startsWith("wateringRecommendations:")) {
                    String[] lines = content.split("\n");

                    for (int i = 1; i < lines.length; i++) { // Omitir la primera línea
                        String[] parts = lines[i].split(": ", 2);
                        if (parts.length == 2) {
                            String dateTimeStr = parts[0]; // Formato: yyyyMMddTHHmmssZ
                            String recommendation = parts[1];

                            int year = Integer.parseInt(dateTimeStr.substring(0, 4));
                            int month = Integer.parseInt(dateTimeStr.substring(4, 6));
                            int day = Integer.parseInt(dateTimeStr.substring(6, 8));

                            wateringDays.add(new WateringDayDTO(day, month, year, recommendation));
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }

        return wateringDays;
    }

    public static PlantIdentifiedDTO toPlantIdentifiedDTO(PlantIdentified plant) {
        PlantIdentifiedDTO dto = new PlantIdentifiedDTO(
                plant.getId(),
                plant.getName(),
                plant.getWatering(),
                plant.getBestWatering(),
                plant.getBestLightCondition(),
                plant.getBestSoilType()
        );
        return dto;
    }

    public static JsonNode getJsonNodeFromResponseBody(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new com.blooming.api.exception.ParsingException("Error parsing response", e);
        }
    }

    public static String getLastAnswerFromResponse(JsonNode rootNode) {
        JsonNode messages = rootNode.path("messages");
        String lastAnswer = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            JsonNode message = messages.get(i);
            if ("answer".equals(message.path("type").asText())) {
                lastAnswer = message.path("content").asText();
                break;
            }
        }
        return Objects.requireNonNullElse(lastAnswer, "No answer found.");
    }

    public static String parseWateringDatesToString(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponseDTO apiResponse = objectMapper.readValue(response.getBody(), ApiResponseDTO.class);

            for (MessageDTO message : apiResponse.getMessages()) {
                if ("answer".equals(message.getType()) && message.getContent().startsWith("wateringSchedule:")) {
                    return message.getContent().replace("wateringSchedule:", "").trim();
                }
            }
        } catch (Exception e) {
            throw new com.blooming.api.exception.ParsingException("Error parsing JSON response", e);
        }
        return null;
    }
}
