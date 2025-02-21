package com.blooming.api.controller;

import com.blooming.api.entity.*;
import com.blooming.api.request.QuestionRequest;
import com.blooming.api.response.dto.PlantIdentifiedDTO;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.WateringPlanDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import com.blooming.api.service.google.FileGeneratorService;
import com.blooming.api.service.plant.PlantIdentifiedService;
import com.blooming.api.service.plantAI.IPlantAIService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    public DrPlantaController(IPlantAIService plantIdService,
                              IUserService userService,
                              FileGeneratorService fileGeneratorService,
                              WateringPlanService wateringPlanService,
                              PlantIdentifiedService plantIdentifiedService,
                              JwtService jwtService) {
        this.plantIdService = plantIdService;
        this.userService = userService;
        this.fileGeneratorService = fileGeneratorService;
        this.wateringPlanService = wateringPlanService;
        this.plantIdentifiedService = plantIdentifiedService;
        this.jwtService = jwtService;
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
    @PostMapping("/savePlantIdentifiedByUser/{tokenPlant}/{plantName}")
    public ResponseEntity<?> savePlantIdentifiedByUser(@PathVariable("plantName") String plantName,
                                                       @PathVariable("tokenPlant") String tokenPlant,
                                                       HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return savePlantIdentified(plantName, tokenPlant, userEmail, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PostMapping("/savePlantIdentifiedByAdmin/{plantName}/{userEmail}/{tokenPlant}")
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


    @PreAuthorize("hasAnyRole('DESIGNER_USER', 'SIMPLE_USER')")
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

            List<String> wateringDates = plantIdService.generateWateringDates(tokenPlant);
            fileGeneratorService.generateGoogleCalendarFile(wateringDates);

            List<WateringDayDTO> wateringDays = plantIdService.generateWateringDays(tokenPlant, wateringDates);
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
    public ResponseEntity<byte[]> generateWateringPlanPdf(@PathVariable("id") Long wateringPlanId) {
        WateringPlan wateringPlan = wateringPlanService.getWateringPlanById(wateringPlanId);
        byte[] pdfBytes = fileGeneratorService.generateWateringPlanPdf(wateringPlan);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=watering_plan_" + wateringPlanId + ".pdf");
        headers.add("Content-Type", "application/pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @GetMapping("/getWateringPlansByUser")
    public ResponseEntity<?> getWateringPlansByUser(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isPresent()) {
            return getWateringPlansByUserId(user.get().getId(), page, size, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @GetMapping("/getWateringPlansByUserAdmin/{userEmail}")
    public ResponseEntity<?> getWateringPlansByUserAdmin(@PathVariable String userEmail,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         HttpServletRequest request) {
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isPresent()) {
            return getWateringPlansByUserId(user.get().getId(), page, size, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    private ResponseEntity<?> getWateringPlansByUserId(Long userId, int page, int size, HttpServletRequest request) {
        Page<WateringPlanDTO> wateringPlanPage = wateringPlanService.getWateringPlansByUser(userId, page, size);
        return getPaginatedResponse(wateringPlanPage, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @GetMapping("/getPlantsByUserAdmin/{userEmail}")
    public ResponseEntity<?> getPlantsByUserAdmin(@PathVariable String userEmail,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        return getPlantsByUserEmail(userEmail, page, size, request, false);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @GetMapping("/getPlantsByUser")
    public ResponseEntity<?> getPlantsByUser(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return getPlantsByUserEmail(userEmail, page, size, request, true);
    }

    private ResponseEntity<?> getPlantsByUserEmail(String userEmail, int page, int size, HttpServletRequest request, boolean activeOnly) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            Page<PlantIdentifiedDTO> plantPage;
            if (activeOnly) {
                plantPage = plantIdentifiedService.getAllActivePlantsByUser(user.get().getId(), page, size);
            } else {
                plantPage = plantIdentifiedService.getAllPlantsByUser(user.get().getId(), page, size);
            }
            return getPaginatedResponse(plantPage, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasRole('ADMIN_USER')")
    @PatchMapping("/activatePlant/{id}")
    public ResponseEntity<?> activatePlantIdentified(@PathVariable("id") Long plantId, HttpServletRequest request) {
        return changePlantStatus(plantId, EntityStatus.ACTIVATE, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PatchMapping("/deactivatePlant/{id}")
    public ResponseEntity<?> deactivatePlantIdentified(@PathVariable("id") Long plantId, HttpServletRequest request) {
        return changePlantStatus(plantId, EntityStatus.DEACTIVATE, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PatchMapping("/addImageToWateringDay/{wateringDayId}")
    public ResponseEntity<?> addImageToWateringDay(
            @PathVariable Long wateringDayId,
            @RequestParam("img") MultipartFile image,
            HttpServletRequest request) {

        WateringDay updatedWateringDay = wateringPlanService.addImageToWateringDay(wateringDayId, image);
        return new GlobalHandlerResponse()
                .handleResponse(HttpStatus.OK.name(),
                        updatedWateringDay, HttpStatus.OK, request);
    }


    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/askAI/{id}")
    public ResponseEntity<?> askAI(@PathVariable("id") Long plantId,
                                   @Valid @RequestBody QuestionRequest questionRequest,
                                   HttpServletRequest request) {
        PlantIdentified plantIdentified = plantIdentifiedService.getById(plantId);
        String accessToken = plantIdentified.getPlantToken();
        String answer = plantIdService.askPlantId(accessToken, questionRequest.question());
        return new GlobalHandlerResponse().handleResponse(HttpStatus.OK.name(),
                answer, HttpStatus.OK, request);
    }

    private ResponseEntity<?> changePlantStatus(Long plantId, EntityStatus status, HttpServletRequest request) {
        boolean success;
        boolean wateringPlansSuccess;
        String actionMessage = status == EntityStatus.ACTIVATE ? "activated successfully" : "deactivated successfully";

        switch (status) {
            case ACTIVATE:
                success = plantIdentifiedService.activate(plantId);
                if (success) {
                    PlantIdentified plant = plantIdentifiedService.getById(plantId);
                    wateringPlansSuccess = wateringPlanService.activateWateringPlans(plant);
                } else {
                    return new GlobalHandlerResponse()
                            .handleResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR, request);
                }
                break;

            case DEACTIVATE:
                success = plantIdentifiedService.deactivate(plantId);
                if (success) {
                    PlantIdentified plant = plantIdentifiedService.getById(plantId);
                    wateringPlansSuccess = wateringPlanService.deactivateWateringPlans(plant);
                } else {
                    return new GlobalHandlerResponse()
                            .handleResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR, request);
                }
                break;

            default:
                return new GlobalHandlerResponse()
                        .handleResponse(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST, request);
        }

        if (wateringPlansSuccess) {
            return new GlobalHandlerResponse()
                    .handleResponse(HttpStatus.OK.name(), "Plant and watering plans " + actionMessage, HttpStatus.OK, request);
        } else {
            return new GlobalHandlerResponse()
                    .handleResponse(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST, request);
        }
    }


    private ResponseEntity<?> getPaginatedResponse(Page<?> page, HttpServletRequest request) {
        if (page.isEmpty()) {
            return new GlobalHandlerResponse().handleResponse(HttpStatus.NO_CONTENT.name(),
                    page.getContent(), HttpStatus.OK, request);
        }

        MetaResponse metaResponse = new MetaResponse(
                request.getRequestURL().toString(),
                request.getMethod(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
        return new GlobalHandlerResponse().handleResponse(HttpStatus.OK.name(), page.getContent(), HttpStatus.OK, metaResponse, request);
    }

}
