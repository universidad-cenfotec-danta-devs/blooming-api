package com.blooming.api.service.plantAI;

import com.blooming.api.exception.ParsingException;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.PlantDetailsDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.blooming.api.utils.ParsingUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        return ParsingUtils.parsePlantSuggestions(response.getBody())
                .orElseThrow(() -> new ParsingException("Error parsing plant suggestion"));
    }


    @Override
    public PlantDetailsDTO getPlantInformationByName(String plantName, String idAccessToken) {
        String accessTokenForDetails = searchPlantByScientificName(plantName)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found with scientific name: " + plantName));
        var headers = createHeaders();
        var requestEntity = new HttpEntity<>(headers);
        String url = apiPlantBaseUrl + accessTokenForDetails + DETAIL_PARAMS;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        PlantDetailsDTO plantDetailsDTO = ParsingUtils.parsePlantDetails(response.getBody())
                .orElseThrow(() -> new ParsingException("Error parsing plant: " + plantName));

        if (plantDetailsDTO.getWatering() == null) {
            plantDetailsDTO.setWatering(generateWateringValues(idAccessToken, headers).orElseThrow(() -> new EntityNotFoundException("Watering not found")));
        }
        return plantDetailsDTO;
    }

    private Optional<String> generateWateringValues(String accessToken, HttpHeaders headers) {
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
            JsonNode jsonNode = ParsingUtils.getJsonNodeFromResponseBody(response);
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
    public List<String> generateWateringSchedule(String idAccessToken) {
        String url = apiIdentifyUrl + "/" + idAccessToken + "/conversation";
        String currentDate = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
        String jsonBody = String.format("""
                {
                    "question": "Generate a watering schedule for the next 2 months for this plant at 3pm, using the watering, bestWatering, bestLightCondition, and bestSoilType values. Only include dates and times in the format yyyyMMddTHHmmssZ. Ensure all dates are strictly after the current date (today's date, %s). Start answer content with text wateringSchedule: ",
                    "prompt": "Only include dates in format yyyyMMddTHHmmssZ that are after today. No extra text. No special characters like asterisks. Start answer content with text wateringSchedule: ",
                    "created": "%s"
                }
                """, currentDate, currentDate);

        var requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        String wateringDates = ParsingUtils.parseWateringDatesToString(response);
        assert wateringDates != null;
        return Arrays.asList(wateringDates.trim().split("\n"));
    }

    @Override
    public List<WateringDayDTO> generateWateringDays(String idAccessToken, List<String> wateringDates) {
        String url = apiIdentifyUrl + "/" + idAccessToken + "/conversation";

        String jsonBody = String.format("""
                {
                    "question": "Start content with value wateringRecommendations: then generate recommendations for each date based on this list generated for watering using the watering, bestWatering, bestLightCondition, and bestSoilType values and all of the others. %s",
                    "prompt": "Only include dates in format yyyy-MM-dd that are after today. No extra text. No special characters like asterisks. Start answer content with text wateringRecommendations: "
                }
                """, wateringDates);

        var requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return ParsingUtils.parseWateringDays(response);
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



    private HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            set(API_KEY_HEADER, apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }

}
