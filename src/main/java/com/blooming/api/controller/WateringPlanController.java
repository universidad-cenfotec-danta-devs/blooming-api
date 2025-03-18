package com.blooming.api.controller;

import com.blooming.api.entity.PlantIdentified;
import com.blooming.api.entity.User;
import com.blooming.api.entity.WateringDay;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.dto.WateringPlanDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.google.FileGeneratorService;
import com.blooming.api.service.plant.PlantIdentifiedService;
import com.blooming.api.service.plantAI.IPlantAIService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import com.blooming.api.utils.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/wateringPlan")
public class WateringPlanController {

    private final JwtService jwtService;
    private final IUserService userService;
    private final IPlantAIService plantAIService;
    private final PlantIdentifiedService plantIdentifiedService;
    private final WateringPlanService wateringPlanService;
    private final FileGeneratorService fileGeneratorService;

    public WateringPlanController(JwtService jwtService, IUserService userService, IPlantAIService plantAIService, PlantIdentifiedService plantIdentifiedService, WateringPlanService wateringPlanService, FileGeneratorService fileGeneratorService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.plantAIService = plantAIService;
        this.plantIdentifiedService = plantIdentifiedService;
        this.fileGeneratorService = fileGeneratorService;
        this.wateringPlanService = wateringPlanService;
    }


    @PreAuthorize("hasAnyRole('DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/generateByUser/{id}")
    public ResponseEntity<?> generateByUser(@PathVariable("id") Long plantId,
                                            HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return generateWateringPlan(plantId, userEmail, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PostMapping("/generateByAdmin/{id}/{userEmail}")
    public ResponseEntity<?> generateByAdmin(@PathVariable("id") Long plantId,
                                             @PathVariable("userEmail") String userEmail,
                                             HttpServletRequest request) {
        return generateWateringPlan(plantId, userEmail, request);
    }

    private ResponseEntity<?> generateWateringPlan(Long plantId, String userEmail, HttpServletRequest request) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            PlantIdentified plantIdentified = plantIdentifiedService.getById(plantId);
            String tokenPlant = plantIdentified.getPlantToken();

            List<String> wateringDates = plantAIService.generateWateringDates(tokenPlant);
            fileGeneratorService.generateGoogleCalendarFile(wateringDates);

            List<WateringDayDTO> wateringDays = plantAIService.generateWateringDays(tokenPlant, wateringDates);
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
    @GetMapping("/generatePDF/{id}")
    public ResponseEntity<byte[]> generatePDF(@PathVariable("id") Long wateringPlanId) {
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


    private ResponseEntity<?> getWateringPlansByUserId(Long userId, int page, int size, HttpServletRequest request) {
        Page<WateringPlanDTO> wateringPlanPage = wateringPlanService.getWateringPlansByUser(userId, page, size);
        return PaginationUtils.getPaginatedResponse(wateringPlanPage, request);
    }

}
