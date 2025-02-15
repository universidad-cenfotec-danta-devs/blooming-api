package com.blooming.api.service.plantAI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class PlantAIService implements IPlantAIService {

    public static final String API_KEY_HEADER = "Api-Key";

    private final RestTemplate restTemplate;

    @Value("${plant.id.api.url}")
    private String apiUrl;

    @Value("${plant.id.api.key}")
    private String apiKey;

    public PlantAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getResponse(byte[] bytesImg) {
        var base64Image = Base64.getEncoder().encodeToString(bytesImg);
        String jsonBody = "{ \"images\": [\"" + base64Image + "\"] }";

        var headers = new HttpHeaders();
        headers.set(API_KEY_HEADER, apiKey);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var requestEntity = new HttpEntity<>(jsonBody, headers);
        var response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }


}
