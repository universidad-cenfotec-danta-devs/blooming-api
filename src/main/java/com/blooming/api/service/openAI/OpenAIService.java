package com.blooming.api.service.openAI;

import com.blooming.api.entity.Canton;
import com.blooming.api.exception.ParsingException;
import com.blooming.api.repository.canton.ICantonRepository;
import com.blooming.api.utils.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class OpenAIService implements IOpenAIService {

    private final ICantonRepository cantonRepository;
    private final RestTemplate restTemplate;

    @Value("${open.ai.api.key}")
    private String apiKey;

    @Value("${open.ai.api.base.url}")
    private String apiUrl;

    public OpenAIService(ICantonRepository cantonRepository, RestTemplate restTemplate) {
        this.cantonRepository = cantonRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getFaunaByLocation(String cantonName) {
        Optional<Canton> canton = cantonRepository.findByName(cantonName.toLowerCase());

        if (canton.isPresent()) {
            return canton.get().getFloraDescription();
        } else {
            String jsonBody = String.format("""
                    {
                        "model": "gpt-3.5-turbo",
                        "messages": [{"role": "user", "content": "Dime en un texto qué tipo de flora se puede encontrar en el cantón de Costa Rica llamado %s."}],
                        "max_tokens": 800
                    }
                    """, cantonName);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, HttpUtils.createHeadersForOpenAI(apiKey));

            ResponseEntity<String> response;
            try {
                response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            } catch (RestClientException e) {
                throw new RuntimeException(e);
            }

            String floraDescription = extractMessage(response);
            Canton newCanton = new Canton();
            newCanton.setName(cantonName.toLowerCase());
            newCanton.setFloraDescription(floraDescription);
            cantonRepository.save(newCanton);
            return floraDescription;
        }
    }

    private String extractMessage(ResponseEntity<String> response) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            JsonNode messageNode = jsonNode.path("choices").path(0).path("message").path("content");
            if (messageNode.isMissingNode()) {
                throw new ParsingException("Content field not found in response");
            }
            return messageNode.asText("Could not get information");
        } catch (JsonProcessingException e) {
            throw new ParsingException("Error processing API response: " + response.getBody());
        }
    }

}
