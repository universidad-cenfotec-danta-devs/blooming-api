package com.blooming.api.service.plantAI;

import com.blooming.api.response.dto.PlantDetailsDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.blooming.api.utils.DTOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PlantAIService implements IPlantAIService {

    public static final String API_KEY_HEADER = "Api-Key";
    public static final String DETAIL_PARAMS = "?details=watering,best_watering,best_light_condition,best_soil_type";

    private final RestTemplate restTemplate;

    @Value("${plant.id.api.identify.url}")
    private String apiIdentifyUrl;

    @Value("${plant.id.api.plant.search.url}")
    private String apiPlantSearchUrl;

    @Value("${plant.id.api.base.url}")
    private String apiPlantBaseUrl;

    @Value("${plant.id.api.key}")
    private String apiKey;

    public PlantAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<PlantSuggestionDTO> identifyImage(byte[] bytesImg) {
        String base64Image = Base64.getEncoder().encodeToString(bytesImg);
        String jsonBody = """
                {
                    "images": ["%s"],
                    "similar_images": true
                }
                """.formatted(base64Image);

        HttpHeaders headers = createHeaders();
        var requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiIdentifyUrl, HttpMethod.POST, requestEntity, String.class);

        return DTOUtils.parsePlantSuggestions(response.getBody())
                .orElseThrow(() -> new IllegalArgumentException("Error parsing plant suggestion"));
    }


    @Override
    public PlantDetailsDTO getPlantInformationByName(String plantName, String idAccessToken) {
        String accessTokenForDetails = searchPlantByScientificName(plantName)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found with scientific name: " + plantName));
        var headers = createHeaders();
        var requestEntity = new HttpEntity<>(headers);
        String url = apiPlantBaseUrl + accessTokenForDetails + DETAIL_PARAMS;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        PlantDetailsDTO plantDetailsDTO = DTOUtils.parsePlantDetails(response.getBody())
                .orElseThrow(() -> new IllegalArgumentException("Error parsing plant: " + plantName));

        if (plantDetailsDTO.getWatering() == null) {
            plantDetailsDTO.setWatering(generateWatering(idAccessToken, headers).orElseThrow(() -> new EntityNotFoundException("Watering not found")));
        }
        return plantDetailsDTO;
    }

    private Optional<String> generateWatering(String accessToken, HttpHeaders headers) {
        String url = apiIdentifyUrl + "/" + accessToken + "/conversation";
        String jsonBody = """
                {
                    "question": "The watering of this plant is dry(1), medium(2) or wet(3)?",
                    "prompt": "Return only the values 'min' and 'max' in valid JSON format. No extra text."
                }
                """;

        var requestEntity = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonNode jsonNode = getJsonNodeFromResponseBody(response);
            JsonNode messages = jsonNode.path("messages");
            String wateringInfo = "";

            for (JsonNode message : messages) {
                if ("answer".equals(message.path("type").asText())) {
                    wateringInfo = message.path("content").asText();
                    break;
                }
            }

            return Optional.ofNullable(wateringInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateWateringSchedule(String idAccessToken) {
        String url = apiIdentifyUrl + "/" + idAccessToken + "/conversation";
        String jsonBody = """
                {
                    "question": "Generate a watering schedule for the next 2 months for this plant, using the watering, bestWatering, bestLightCondition, and bestSoilType values. Only include dates and times",
                    "prompt": "Only include dates. No extra text. No special characters like asterisks"
                }
                """;
        var requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        JsonNode jsonNode = getJsonNodeFromResponseBody(response);
        return "";
    }

    private Optional<String> searchPlantByScientificName(String plantName) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            String url = apiPlantSearchUrl + plantName;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            String accessToken = new ObjectMapper()
                    .readTree(response.getBody())
                    .path("entities")
                    .path(0)
                    .path("access_token")
                    .asText(null);

            return Optional.ofNullable(accessToken);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private JsonNode getJsonNodeFromResponseBody(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response", e);
        }
    }

    private HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            set(API_KEY_HEADER, apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }

}
