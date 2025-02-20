package com.blooming.api.controller;

import com.blooming.api.entity.User;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.response.dto.PlantIdentifiedDTO;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.google.FileGeneratorService;
import com.blooming.api.service.plant.PlantIdentifiedService;
import com.blooming.api.service.plantAI.IPlantAIService;
import com.blooming.api.service.plantAI.PlantAIService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dr")
public class DrPlantaController {

    private final IPlantAIService plantIdService;
    private final IUserService userService;
    private final FileGeneratorService fileGeneratorService;
    private final WateringPlanService wateringPlanService;
    private final PlantIdentifiedService plantIdentifiedService;
    private final JwtService jwtService;
    private final PlantAIService plantAIService;

    public DrPlantaController(IPlantAIService plantIdService,
                              IUserService userService,
                              FileGeneratorService fileGeneratorService,
                              WateringPlanService wateringPlanService,
                              PlantIdentifiedService plantIdentifiedService,
                              JwtService jwtService, PlantAIService plantAIService) {
        this.plantIdService = plantIdService;
        this.userService = userService;
        this.fileGeneratorService = fileGeneratorService;
        this.wateringPlanService = wateringPlanService;
        this.plantIdentifiedService = plantIdentifiedService;
        this.jwtService = jwtService;
        this.plantAIService = plantAIService;
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    @PostMapping("/img")
    public ResponseEntity<?> generatePlantSuggestions(@RequestParam("img") MultipartFile img, HttpServletRequest request) throws IOException {
        byte[] imageBytes = img.getBytes();

        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                plantIdService.identifyImage(imageBytes),
                HttpStatus.OK, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @GetMapping("/savePlantIdentifiedByUser/{tokenPlant}/{plantName}")
    public ResponseEntity<?> savePlantIdentifiedByUser(@PathVariable("plantName") String plantName,
                                                       @PathVariable("tokenPlant") String tokenPlant,
                                                       HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return savePlantIdentified(plantName, tokenPlant, userEmail, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @GetMapping("/savePlantIdentifiedByAdmin/{plantName}/{userEmail}/{tokenPlant}")
    public ResponseEntity<?> savePlantIdentifiedByAdmin(@PathVariable("plantName") String plantName,
                                                        @PathVariable("userEmail") String userEmail,
                                                        @PathVariable("tokenPlant") String tokenPlant,
                                                        HttpServletRequest request) {
        return savePlantIdentified(plantName, tokenPlant, userEmail, request);
    }

    private ResponseEntity<?> savePlantIdentified(String plantName, String tokenPlant, String userEmail, HttpServletRequest request) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            PlantIdentified plantIdentified = plantIdService.getPlantInformationByName(plantName, tokenPlant);
            plantIdentified.setUser(user.get());
            PlantIdentifiedDTO dto = plantIdentifiedService.register(plantIdentified);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    dto,
                    HttpStatus.OK, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    "",
                    HttpStatus.BAD_REQUEST, request);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/generateWateringPlanByUser/{id}")
    public ResponseEntity<?> generateWateringPlanByUser(@PathVariable("id") Long plantId,
                                                        HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return generateWateringPlan(plantId, userEmail, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PostMapping("/generateWateringPlanByAdmin/{id}/{userEmail}")
    public ResponseEntity<?> generateWateringPlanByAdmin(@PathVariable("id") Long plantId,
                                                         @PathVariable("userEmail") String userEmail,
                                                         HttpServletRequest request) {
        return generateWateringPlan(plantId, userEmail, request);
    }

    private ResponseEntity<?> generateWateringPlan(Long plantId, String userEmail, HttpServletRequest request) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            PlantIdentified plantIdentified = plantIdentifiedService.getById(plantId);
            String tokenPlant = plantIdentified.getPlantToken();

            List<String> wateringSchedule = plantIdService.generateWateringSchedule(tokenPlant);
            fileGeneratorService.generateGoogleCalendarFile(wateringSchedule);

            List<WateringDayDTO> wateringDays = plantIdService.generateWateringDays(tokenPlant, wateringSchedule);
            WateringPlan wateringPlan = wateringPlanService.register(wateringDays, plantIdentified);
            fileGeneratorService.generateWateringPlanPdf(wateringPlan);

            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    wateringPlan,
                    HttpStatus.OK, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    "",
                    HttpStatus.BAD_REQUEST, request);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generateWateringPlanPdf(@PathVariable("id") Long id) {
        WateringPlan wateringPlan = wateringPlanService.getWateringPlanById(id);
        byte[] pdfBytes = fileGeneratorService.generateWateringPlanPdf(wateringPlan);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=watering_plan_" + id + ".pdf");
        headers.add("Content-Type", "application/pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/askAI/{idAccessToken}")
    public String askAI(@PathVariable("idAccessToken") String idAccessToken,
                        @PathParam("question") String question) {
        return plantAIService.askPlantId(idAccessToken, question);
    }

}
