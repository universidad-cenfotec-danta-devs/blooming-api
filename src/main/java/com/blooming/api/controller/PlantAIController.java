package com.blooming.api.controller;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.request.LatLongRequest;
import com.blooming.api.request.LogInRequest;
import com.blooming.api.request.QuestionRequest;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.plant.IPlantIdentifiedService;
import com.blooming.api.service.plantAI.IPlantAIService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/plantAI")
public class PlantAIController {


    private final IPlantAIService plantAIService;
    private final IPlantIdentifiedService plantIdentifiedService;

    public PlantAIController(IPlantAIService plantAIService, IPlantIdentifiedService plantIdentifiedService) {
        this.plantAIService = plantAIService;
        this.plantIdentifiedService = plantIdentifiedService;
    }


    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    @PostMapping("/img")
    public ResponseEntity<?> generatePlantSuggestions(@RequestParam("img") MultipartFile img, HttpServletRequest request) throws IOException {
        byte[] imageBytes = img.getBytes();

        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantAIService.identifyImage(imageBytes),
                HttpStatus.OK, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    @PostMapping("/healthAssessment")
    public ResponseEntity<?> generateHealthAssessment(@RequestParam("img") MultipartFile img, HttpServletRequest request) throws IOException {
        byte[] imageBytes = img.getBytes();

        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantAIService.generateHealthAssessment(imageBytes),
                HttpStatus.OK, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/askAI/{id}")
    public ResponseEntity<?> askAI(@PathVariable("id") Long plantId,
                                   @Valid @RequestBody QuestionRequest questionRequest,
                                   HttpServletRequest request) {
        PlantIdentified plantIdentified = plantIdentifiedService.getById(plantId);
        String accessToken = plantIdentified.getPlantToken();
        String answer = plantAIService.askPlantId(accessToken, questionRequest.question());
        return new GlobalHandlerResponse().handleResponse(HttpStatus.OK.name(),
                answer, HttpStatus.OK, request);
    }

}
