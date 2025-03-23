package com.blooming.api.service.plantAI;

import com.blooming.api.exception.ParsingException;
import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.response.dto.HealthAssessmentDTO;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.blooming.api.utils.HttpUtils;
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

import static com.blooming.api.utils.ParsingUtils.parsePlantSuggestions;

@Service
public class PlantAIService implements IPlantAIService {

    public static final String DETAIL_PARAMS = "?details=watering,best_watering,best_light_condition,best_soil_type";

    private final RestTemplate restTemplate;

    @Value("${plant.id.api.identify.url}")
    private String apiIdentifyUrl;

    @Value("${plant.id.api.plant.search.url}")
    private String apiPlantSearchUrl;

    @Value("${plant.id.api.base.url}")
    private String apiPlantBaseUrl;

    @Value("${plant.id.api.plant.health.url}")
    private String apiPlantHealthUrl;

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

        var requestEntity = new HttpEntity<>(jsonBody, generateHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(apiIdentifyUrl, HttpMethod.POST, requestEntity);
        return parsePlantSuggestions(response);
    }

    @Override
    public List<HealthAssessmentDTO> generateHealthAssessment(byte[] bytesImg) {

        String base64Image = Base64.getEncoder().encodeToString(bytesImg);
        String jsonBody = """
                {
                    "images": ["%s"]
                }
                """.formatted(base64Image);
        var requestEntity = new HttpEntity<>(jsonBody, generateHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(apiPlantHealthUrl, HttpMethod.POST, requestEntity);
        return ParsingUtils.parseHealthAssessment(response);
    }

    @Override
    public PlantIdentified getPlantInformationByName(String plantName, String tokenPlant) {
        String accessTokenForDetails = searchPlantByScientificName(plantName).orElseThrow(() -> new EntityNotFoundException("Plant not found with scientific name: " + plantName));
       var requestEntity = new HttpEntity<>(generateHeaders());
        String url = apiPlantBaseUrl + accessTokenForDetails + DETAIL_PARAMS;
        ResponseEntity<String> response = makeRequestToPlantAI(url, HttpMethod.GET, requestEntity);
        return ParsingUtils.parsePlantDetails(response, tokenPlant);
    }

    @Override
    public List<String> generateWateringDates(String idAccessToken) {
        String currentDate = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
        String jsonBody = String.format("""
                {
                    "question": "Generate watering dates for the next 2 months for this plant at 3pm. Use watering, bestWatering, bestLightCondition, and bestSoilType values. Only include dates and times in the format yyyyMMddTHHmmssZ. Ensure all dates are strictly after the current date (today's date, %s). Start answer content with text wateringSchedule: ",
                    "prompt": "Only include dates in format yyyyMMddTHHmmssZ that are after today. Include max of 15 dates. No extra text. No special characters like asterisks. Start answer content with text wateringSchedule: ",
                    "created": "%s"
                }
                """, currentDate, currentDate);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody,generateHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(idAccessToken), HttpMethod.POST, requestEntity);
        String wateringDates = ParsingUtils.parseWateringDatesToString(response);
        return Arrays.asList(wateringDates.split("\n"));
    }

    @Override
    public List<WateringDayDTO> generateWateringDays(String tokenPlant, List<String> wateringDates) {
        String jsonBody = String.format("""
                {
                "question": "Generate for this plant watering recommendations for each date of this list. Try to not repeat recommendations. %s",
                "prompt": "Answer format is date in format yyyy-MM-dd that are after today. In recommendations only include letters no spec characters. Example: yyyy-MM-dd:recommendationHere. Start answer content with text wateringRecommendations: "
                }
                """, wateringDates);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, generateHeaders());
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(tokenPlant), HttpMethod.POST, requestEntity);
        return ParsingUtils.parseWateringDays(response);
    }

    @Override
    public String askPlantId(String plantToken, String question) {
        String jsonBody = String.format("""
                {
                    "question": "%s",
                    "prompt": "Respond in spanish language. No extra text. No special characters like asterisks."
                }
                """, question);

        HttpHeaders headers = generateHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = makeRequestToPlantAI(buildAskPlantIdUrl(plantToken), HttpMethod.POST, requestEntity);

        JsonNode jsonNode = ParsingUtils.getJsonNodeFromResponseBody(response);
        return ParsingUtils.getLastAnswerFromResponse(jsonNode);
    }

    private Optional<String> searchPlantByScientificName(String plantName) {

        HttpHeaders headers = HttpUtils.createHeadersForPlantId(apiKey);
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

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 10000, multiplier = 2))
    private ResponseEntity<String> makeRequestToPlantAI(String url, HttpMethod method, HttpEntity<?> requestEntity) {
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }

    private HttpHeaders generateHeaders() {
        return HttpUtils.createHeadersForPlantId(apiKey);
    }

}
