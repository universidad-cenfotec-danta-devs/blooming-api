package com.blooming.api.service.plantAI;

import com.blooming.api.exception.ParsingException;
import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.blooming.api.utils.ParsingUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PlantAIService implements IPlantAIService {

    public static final String API_KEY_HEADER = "Api-Key";
    public static final String DETAIL_PARAMS = "?details=watering,best_watering,best_light_condition,best_soil_type&language=es";

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
        ResponseEntity<String> response = makeRequestToPlantAI(apiIdentifyUrl, HttpMethod.POST, requestEntity);
        return ParsingUtils.parsePlantSuggestions(response);
    }


    @Override
    public PlantIdentified getPlantInformationByName(String plantName, String tokenPlant) {
        String accessTokenForDetails = searchPlantByScientificName(plantName).orElseThrow(() -> new EntityNotFoundException("Plant not found with scientific name: " + plantName));
        var headers = createHeaders();
        var requestEntity = new HttpEntity<>(headers);
        String url = apiPlantBaseUrl + accessTokenForDetails + DETAIL_PARAMS;
        ResponseEntity<String> response = makeRequestToPlantAI(url, HttpMethod.GET, requestEntity);
        //TODO: String s3ImageURL= S3Service.saveImage(...)
        //TODO: plantIdentified.setImageURL(s3ImageURL)

        return ParsingUtils.parsePlantDetails(response, tokenPlant);
    }

    @Override
    public List<String> generateWateringSchedule(String idAccessToken) {
        String currentDate = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
        String jsonBody = String.format("""
                {
                    "question": "Generate a watering schedule for the next 2 months for this plant at 3pm. Base answer in watering, bestWatering, bestLightCondition, and bestSoilType values. Only include dates and times in the format yyyyMMddTHHmmssZ. Ensure all dates are strictly after the current date (today's date, %s). Start answer content with text wateringSchedule: ",
                    "prompt": "Only include dates in format yyyyMMddTHHmmssZ that are after today. Include max of 15 dates. No extra text. No special characters like asterisks. Start answer content with text wateringSchedule: ",
                    "created": "%s"
                }
                """, currentDate, currentDate);

        var requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(idAccessToken), HttpMethod.POST, requestEntity);
        String wateringDates = ParsingUtils.parseWateringDatesToString(response);
        return Arrays.asList(wateringDates.trim().split("\n"));
    }

    @Override
    public List<WateringDayDTO> generateWateringDays(String tokenPlant, List<String> wateringDates) {
        String jsonBody = String.format("""
                {
                    "question": "Start content with value wateringRecommendations: then generate recommendations for each date based on this list generated for watering using the watering, bestWatering, bestLightCondition, and bestSoilType values and all of the others. %s",
                    "prompt": "Only include dates in format yyyy-MM-dd that are after today. Do not include any words or text other than the date. Do not use any characters like 'El', asterisks, or anything extra. Start answer content with text wateringRecommendations: "
                }
                """, wateringDates);

        var requestEntity = new HttpEntity<>(jsonBody, createHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(tokenPlant), HttpMethod.POST, requestEntity);
        return ParsingUtils.parseWateringDays(response);
    }

    @Override
    public String askPlantId(String plantToken, String question) {
        String jsonBody = String.format("""
                {
                    "question": "%s",
                    "prompt": "No extra text. No special characters like asterisks."
                }
                """, question);

        HttpHeaders headers = createHeaders();
        var requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(plantToken), HttpMethod.POST, requestEntity);

        JsonNode jsonNode = ParsingUtils.getJsonNodeFromResponseBody(response);
        return ParsingUtils.getLastAnswerFromResponse(jsonNode);
    }


    private Optional<String> searchPlantByScientificName(String plantName) {

        HttpHeaders headers = createHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String url = apiPlantSearchUrl + plantName;
        ResponseEntity<String> response = makeRequestToPlantAI(url, HttpMethod.GET, requestEntity);

        try {
            String accessToken = new ObjectMapper().readTree(response.getBody()).path("entities").path(0).path("access_token").asText(null);
            return Optional.ofNullable(accessToken);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e.getMessage());
        }
    }

    private String buildAskPlantIdUrl(String token) {
        return apiIdentifyUrl + "/" + token + "/conversation";
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 10000))
    private ResponseEntity<String> makeRequestToPlantAI(String url, HttpMethod method, HttpEntity<?> requestEntity) {
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }


    private HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            set(API_KEY_HEADER, apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }

}
