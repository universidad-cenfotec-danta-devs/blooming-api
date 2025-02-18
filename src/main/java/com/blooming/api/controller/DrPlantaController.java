package com.blooming.api.controller;

import com.blooming.api.entity.User;
import com.blooming.api.entity.WateringPlan;
import com.blooming.api.response.dto.WateringDayDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.google.CalendarFileGenerator;
import com.blooming.api.service.plantAI.IPlantAIService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final CalendarFileGenerator calendarFileGenerator;
    private final WateringPlanService wateringPlanService;
    private final JwtService jwtService;

    public DrPlantaController(IPlantAIService plantIdService, IUserService userService, CalendarFileGenerator calendarFileGenerator, WateringPlanService wateringPlanService, JwtService jwtService) {
        this.plantIdService = plantIdService;
        this.userService = userService;
        this.calendarFileGenerator = calendarFileGenerator;
        this.wateringPlanService = wateringPlanService;
        this.jwtService = jwtService;
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

        String username = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        Optional<User> user = userService.findByEmail(username);

        if (user.isPresent()) {
            List<String> wateringSchedule = plantIdService.generateWateringSchedule(idAccessToken);
            calendarFileGenerator.generateWateringScheduleFile(wateringSchedule);
            List<WateringDayDTO> wateringDays = plantIdService.generateRecommendationsForEachDay(idAccessToken, wateringSchedule);
            WateringPlan wateringPlan = wateringPlanService.register(wateringDays, user.get());
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


}
