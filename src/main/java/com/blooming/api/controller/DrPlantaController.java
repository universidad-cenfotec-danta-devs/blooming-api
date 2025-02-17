package com.blooming.api.controller;

import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.plantAI.IPlantAIService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/dr")
public class DrPlantaController {

    private final IPlantAIService plantIdService;

    public DrPlantaController(IPlantAIService plantIdService) {
        this.plantIdService = plantIdService;
    }

    @PostMapping("/img")
    public ResponseEntity<?> processImg(@RequestParam("img") MultipartFile img, HttpServletRequest request) throws IOException {
        byte[] imageBytes = img.getBytes();

        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantIdService.identifyImage(imageBytes),
                HttpStatus.OK, request);
    }

    @GetMapping("/plantSearch/{idAccessToken}")
    public ResponseEntity<?> getPlantInformationByName(@RequestParam("plantName") String plantName,
                                                       @PathVariable("idAccessToken") String idAccessToken,
                                                       HttpServletRequest request) {
        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantIdService.getPlantInformationByName(plantName, idAccessToken),
                HttpStatus.OK, request);
    }


    @PostMapping("/generateSchedule/{idAccessToken}")
    public ResponseEntity<?> generateWateringPlan(@PathVariable("idAccessToken") String idAccessToken,
                                                  HttpServletRequest request) {
        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantIdService.generateWateringSchedule(idAccessToken),
                HttpStatus.OK, request);
    }


}
