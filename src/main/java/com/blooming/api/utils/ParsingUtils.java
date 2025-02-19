package com.blooming.api.utils;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.exception.ParsingException;
import com.blooming.api.response.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParsingUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MESSAGES = "messages";
    private static final String CONTENT = "content";
    private static final String WATERING_RECOMMENDATIONS = "wateringRecommendations:";
    private static final String WATERING_SCHEDULE = "wateringSchedule:";
    private static final String ANSWER = "answer";
    private static final String RESULT = "result";
    private static final String SUGGESTIONS = "suggestions";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SIMILAR_IMAGES = "similar_images";
    private static final String URL = "url";
    private static final String URL_SMALL = "url_small";
    private static final String PROBABILITY = "probability";
    private static final String SIMILARITY = "similarity";
    private static final String NAME = "name";
    private static final String WATERING = "watering";
    private static final String BEST_WATERING = "best_watering";
    private static final String BEST_LIGHT_CONDITION = "best_light_condition";
    private static final String BEST_SOIL_TYPE = "best_soil_type";

    public static List<PlantSuggestionDTO> parsePlantSuggestions(ResponseEntity<String> response) {
        try {
            JsonNode jsonNode = getJsonNodeFromResponseBody(response);

            List<PlantSuggestionDTO> suggestionsList = new ArrayList<>();
            String idAccessToken = jsonNode.path(ACCESS_TOKEN).asText("");

            JsonNode suggestions = jsonNode.path(RESULT).path("classification").path(SUGGESTIONS);
            if (suggestions.isArray()) {
                for (JsonNode suggestion : suggestions) {
                    String name = suggestion.path(NAME).asText("");
                    double probability = suggestion.path(PROBABILITY).asDouble(0.0);
                    String probabilityPercentage = String.format("%.0f%%", probability * 100);

                    JsonNode firstImage = suggestion.path(SIMILAR_IMAGES).path(0);
                    String imageUrl = firstImage.path(URL).asText("");
                    String imageUrlSmall = firstImage.path(URL_SMALL).asText("");
                    double similarity = firstImage.path(SIMILARITY).asDouble(0.0);
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
            }
            return suggestionsList;
        } catch (Exception e) {
            throw new ParsingException("Error parsing plant suggestions", e);
        }
    }

    public static PlantIdentified parsePlantDetails(ResponseEntity<String> response, String plantToken) {
        try {
            JsonNode plantDetailsNode = getJsonNodeFromResponseBody(response);

            String name = plantDetailsNode.path(NAME).isNull() ? null : plantDetailsNode.path(NAME).asText();
            String watering = plantDetailsNode.path(WATERING).isNull() ? null : plantDetailsNode.path(WATERING).toString();
            String bestWatering = plantDetailsNode.path(BEST_WATERING).isNull() ? null : plantDetailsNode.path(BEST_WATERING).asText();
            String bestLightCondition = plantDetailsNode.path(BEST_LIGHT_CONDITION).isNull() ? null : plantDetailsNode.path(BEST_LIGHT_CONDITION).asText();
            String bestSoilType = plantDetailsNode.path(BEST_SOIL_TYPE).isNull() ? null : plantDetailsNode.path(BEST_SOIL_TYPE).asText();

            PlantIdentified plantIdentified = new PlantIdentified();
            plantIdentified.setPlantToken(plantToken);
            plantIdentified.setName(name);
            plantIdentified.setWatering(watering);
            plantIdentified.setBestWatering(bestWatering);
            plantIdentified.setBestLightCondition(bestLightCondition);
            plantIdentified.setBestSoilType(bestSoilType);

            return plantIdentified;

        } catch (Exception e) {
            throw new ParsingException("Error parsing plant details: " + e.getMessage());
        }
    }

    public static List<WateringDayDTO> parseWateringDays(ResponseEntity<String> response) {
        List<WateringDayDTO> wateringDays = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode messages = root.path(MESSAGES);

            for (JsonNode message : messages) {
                String content = message.path(CONTENT).asText();
                if (content.startsWith(WATERING_RECOMMENDATIONS)) {
                    String[] lines = content.split("\n");

                    for (int i = 1; i < lines.length; i++) { // Skip first line
                        String[] parts = lines[i].split(": ", 2);
                        if (parts.length == 2) {
                            String dateTimeStr = parts[0]; // Format: yyyyMMddTHHmmssZ
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
        try {
            return new PlantIdentifiedDTO(
                    plant.getId(),
                    plant.getName(),
                    plant.getWatering(),
                    plant.getBestWatering(),
                    plant.getBestLightCondition(),
                    plant.getBestSoilType()
            );
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }

    }

    public static JsonNode getJsonNodeFromResponseBody(ResponseEntity<String> response) {
        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new ParsingException("Error parsing response", e);
        }
    }

    public static String getLastAnswerFromResponse(JsonNode rootNode) {
        JsonNode messages = rootNode.path(MESSAGES);
        String lastAnswer = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            JsonNode message = messages.get(i);
            if (ANSWER.equals(message.path("type").asText())) {
                lastAnswer = message.path(CONTENT).asText();
                break;
            }
        }
        return Objects.requireNonNullElse(lastAnswer, "No answer found.");
    }

    public static String parseWateringDatesToString(ResponseEntity<String> response) {
        try {
            ApiResponseDTO apiResponse = objectMapper.readValue(response.getBody(), ApiResponseDTO.class);

            for (MessageDTO message : apiResponse.getMessages()) {
                if (ANSWER.equals(message.getType()) && message.getContent().startsWith(WATERING_SCHEDULE)) {
                    return message.getContent().replace(WATERING_SCHEDULE, "").trim();
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Error parsing JSON response", e);
        }
        return "";
    }
}
