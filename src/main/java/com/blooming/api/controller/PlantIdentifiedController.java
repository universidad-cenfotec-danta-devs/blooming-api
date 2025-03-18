package com.blooming.api.controller;

import com.blooming.api.entity.*;
import com.blooming.api.response.dto.PlantIdentifiedDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.plant.PlantIdentifiedService;
import com.blooming.api.service.plantAI.IPlantAIService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import com.blooming.api.utils.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/plant")
public class PlantIdentifiedController {

    private final IPlantAIService plantAIService;
    private final IUserService userService;
    private final WateringPlanService wateringPlanService;
    private final PlantIdentifiedService plantIdentifiedService;
    private final JwtService jwtService;

    public PlantIdentifiedController(IPlantAIService plantAIService,
                                     IUserService userService,
                                     WateringPlanService wateringPlanService,
                                     PlantIdentifiedService plantIdentifiedService,
                                     JwtService jwtService) {
        this.plantAIService = plantAIService;
        this.userService = userService;
        this.wateringPlanService = wateringPlanService;
        this.plantIdentifiedService = plantIdentifiedService;
        this.jwtService = jwtService;
    }


    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping("/saveByUser/{tokenPlant}/{plantName}")
    public ResponseEntity<?> saveByUser(@PathVariable("plantName") String plantName,
                                        @PathVariable("tokenPlant") String tokenPlant,
                                        HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        return savePlantIdentified(plantName, tokenPlant, userEmail, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PostMapping("/saveByAdmin/{plantName}/{userEmail}/{tokenPlant}")
    public ResponseEntity<?> saveByAdmin(@PathVariable("plantName") String plantName,
                                         @PathVariable("userEmail") String userEmail,
                                         @PathVariable("tokenPlant") String tokenPlant,
                                         HttpServletRequest request) {
        return savePlantIdentified(plantName, tokenPlant, userEmail, request);
    }

    private ResponseEntity<?> savePlantIdentified(String plantName, String tokenPlant, String userEmail, HttpServletRequest request) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            PlantIdentified plantIdentified = plantAIService.getPlantInformationByName(plantName, tokenPlant);
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
            return PaginationUtils.getPaginatedResponse(plantPage, request);
        } else {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasRole('ADMIN_USER')")
    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activatePlantIdentified(@PathVariable("id") Long plantId, HttpServletRequest request) {
        return changePlantStatus(plantId, EntityStatus.ACTIVATE, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivatePlantIdentified(@PathVariable("id") Long plantId, HttpServletRequest request) {
        return changePlantStatus(plantId, EntityStatus.DEACTIVATE, request);
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

}
