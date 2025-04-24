package com.blooming.api.utils;

import com.blooming.api.entity.*;
import com.blooming.api.exception.ParsingException;
import com.blooming.api.response.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.*;

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

    public static List<HealthAssessmentDTO> parseHealthAssessment(ResponseEntity<String> response) {
        try {
            JsonNode rootNode = getJsonNodeFromResponseBody(response);

            List<HealthAssessmentDTO> list = new ArrayList<>();

            JsonNode diseaseNode = rootNode.path("result").path("disease").path("suggestions");

            for (JsonNode suggestionNode : diseaseNode) {
                DiseaseSuggestionDTO diseaseSuggestion = new DiseaseSuggestionDTO();

                diseaseSuggestion.setId(suggestionNode.path("id").asText());
                diseaseSuggestion.setName(suggestionNode.path("name").asText());
                int probability = (int) (suggestionNode.path("probability").asDouble() * 100);
                diseaseSuggestion.setProbability(probability);
                DiseaseDetailsDTO diseaseDetails = new DiseaseDetailsDTO();
                JsonNode detailsNode = suggestionNode.path("details");

                diseaseDetails.setLocalName(detailsNode.path("local_name").asText());
                diseaseDetails.setDescription(detailsNode.path("description").asText());
                diseaseDetails.setUrl(detailsNode.path("url").asText());

                TreatmentDTO treatment = new TreatmentDTO();
                JsonNode treatmentNode = detailsNode.path("treatment");

                treatment.setChemical(parseTreatmentList(treatmentNode.path("chemical")));
                treatment.setBiological(parseTreatmentList(treatmentNode.path("biological")));
                treatment.setPrevention(parseTreatmentList(treatmentNode.path("prevention")));

                diseaseDetails.setTreatment(treatment);

                diseaseSuggestion.setDetails(diseaseDetails);

                HealthAssessmentDTO healthAssessmentDTO = new HealthAssessmentDTO();
                healthAssessmentDTO.setDiseaseSuggestions(List.of(diseaseSuggestion));

                list.add(healthAssessmentDTO);
            }

            return list;

        } catch (Exception e) {
            throw new ParsingException("Error parsing health assessment: " + e.getMessage());
        }
    }

    private static List<String> parseTreatmentList(JsonNode treatmentNode) {
        List<String> treatmentList = new ArrayList<>();
        if (treatmentNode.isArray()) {
            for (JsonNode node : treatmentNode) {
                treatmentList.add(node.asText());
            }
        }
        return treatmentList;
    }


    public static PlantIdentified parsePlantDetails(ResponseEntity<String> response, String plantToken) {
        try {
            JsonNode plantDetailsNode = getJsonNodeFromResponseBody(response);

            String name = plantDetailsNode.path(NAME).isNull() ? null : plantDetailsNode.path(NAME).asText();

            PlantIdentified plantIdentified = new PlantIdentified();
            plantIdentified.setPlantToken(plantToken);
            plantIdentified.setName(name);
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
                            String dateTimeStr = parts[0].trim(); // Remueve espacios en blanco
                            String recommendation = parts[1];

                            // Validar si la fecha tiene al menos 8 caracteres numéricos
                            if (dateTimeStr.matches("\\d{8}.*")) {
                                int year = Integer.parseInt(dateTimeStr.substring(0, 4));
                                int month = Integer.parseInt(dateTimeStr.substring(4, 6));
                                int day = Integer.parseInt(dateTimeStr.substring(6, 8));

                                wateringDays.add(new WateringDayDTO(day, month, year, recommendation));
                            } else {
                                throw new ParsingException("Formato de fecha inválido: " + dateTimeStr);
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Error parsing WateringDays: " + e.getMessage());
        }

        return wateringDays;
    }


    public static PlantIdentifiedDTO toPlantIdentifiedDTO(PlantIdentified plant) {
        try {
            return new PlantIdentifiedDTO(
                    plant.getId(),
                    plant.getName(),
                    plant.isActive()
            );
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }

    }

    public static NurseryDTO toNurseryDTO(Nursery nursery) {
        try {
            NurseryDTO dto = new NurseryDTO();
            dto.setId(nursery.getId());
            dto.setName(nursery.getName());
            dto.setDescription(nursery.getDescription());
            dto.setImageUrl(nursery.getImageUrl());
            dto.setLatitude(nursery.getLatitude());
            dto.setLongitude(nursery.getLongitude());
            dto.setActive(nursery.isStatus());
            dto.setCreatedAt(nursery.getCreatedAt());
            dto.setUpdatedAt(nursery.getUpdatedAt());
            dto.setUserEmail(nursery.getNurseryAdmin().getEmail());
            return dto;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }
    }

    public static PotDTO toPotDTO(Pot pot) {
        try {
            PotDTO dto = new PotDTO();
            dto.setId(pot.getId());
            dto.setName(pot.getName());
            dto.setPrice(pot.getPrice());
            dto.setFileUrl(pot.getImageUrl());//3d file
            dto.setOwnerId(pot.getDesigner().getId());
            dto.setCreatedAt(pot.getCreatedAt());
            dto.setUpdatedAt(pot.getUpdatedAt());
            return dto;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }

    }

    public static ProductDTO toProductDTO(Product product) {
        try {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            return dto;
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

    public static WateringPlanDTO toWateringPlanDTO(WateringPlan wateringPlan) {
        return new WateringPlanDTO(
                wateringPlan.getId(),
                wateringPlan.getPlant().getId(),
                wateringPlan.isActive()
        );
    }

    public static EvaluationDTO toEvaluationDTO(Evaluation savedEvaluation) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdAt = dateFormat.format(savedEvaluation.getCreatedAt());

        String userName = savedEvaluation.getUser() != null ? savedEvaluation.getUser().getName() : "Unknown User";

        return new EvaluationDTO(
                savedEvaluation.getRating(),
                savedEvaluation.getComment(),
                createdAt,
                userName
        );
    }
}
