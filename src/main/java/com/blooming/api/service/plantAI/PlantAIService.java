package com.blooming.api.service.plantAI;

import com.blooming.api.response.dto.PlantDetailsDTO;
import com.blooming.api.response.dto.PlantSuggestionDTO;
import com.blooming.api.utils.DTOUtils;
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
    public PlantDetailsDTO getPlantInformationByName(String plantName) {
        String tokenPlant = searchPlantByScientificName(plantName)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found with scientific name: " + plantName));
        var headers = createHeaders();
        var requestEntity = new HttpEntity<>(headers);
        String url = apiPlantBaseUrl + tokenPlant + DETAIL_PARAMS;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return DTOUtils.parsePlantDetails(response.getBody())
                .orElseThrow(() -> new IllegalArgumentException("Error parsing plant: " + plantName));

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
