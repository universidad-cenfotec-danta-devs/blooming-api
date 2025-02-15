package com.blooming.api.controller;

import com.blooming.api.service.plantAI.IPlantAIService;
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
    public ResponseEntity<String> processImg(@RequestParam("img") MultipartFile img) throws IOException {
        byte[] imageBytes = img.getBytes();
        String result = plantIdService.getResponse(imageBytes);
        return ResponseEntity.ok(result);
    }


}
