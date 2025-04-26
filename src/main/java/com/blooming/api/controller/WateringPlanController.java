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
import com.blooming.api.service.s3.IS3Service;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.service.watering.WateringPlanService;
import com.blooming.api.utils.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@RequestMapping("/api/wateringPlan")
public class WateringPlanController {

    private final JwtService jwtService;
    private final IUserService userService;
    private final IPlantAIService plantAIService;
    private final PlantIdentifiedService plantIdentifiedService;
    private final WateringPlanService wateringPlanService;
    private final FileGeneratorService fileGeneratorService;
    private final IS3Service s3Service;

    public WateringPlanController(JwtService jwtService, IUserService userService, IPlantAIService plantAIService, PlantIdentifiedService plantIdentifiedService, WateringPlanService wateringPlanService, FileGeneratorService fileGeneratorService, IS3Service s3Service) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.plantAIService = plantAIService;
        this.plantIdentifiedService = plantIdentifiedService;
        this.fileGeneratorService = fileGeneratorService;
        this.wateringPlanService = wateringPlanService;
        this.s3Service = s3Service;
    }

    @PostMapping("/generate/{id}")
    public ResponseEntity<?> generate(@PathVariable("id") Long plantId,
                                      HttpServletRequest request) {
        return generateWateringPlan(plantId);
    }

    private ResponseEntity<byte[]> generateWateringPlan(Long plantId) {
        try {
            PlantIdentified plantIdentified = plantIdentifiedService.getById(plantId);
            String tokenPlant = plantIdentified.getPlantToken();

            List<String> wateringDates = plantAIService.generateWateringDates(tokenPlant);
            byte[] calendarFile = fileGeneratorService.generateGoogleCalendarFile(wateringDates);

            List<WateringDayDTO> wateringDays = plantAIService.generateWateringDays(tokenPlant, wateringDates);
            WateringPlan wateringPlan = wateringPlanService.register(wateringDays, plantIdentified);
            byte[] pdfFile = fileGeneratorService.generateWateringPlanPdf(wateringPlan);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                zipOut.putNextEntry(new ZipEntry("watering_schedule.ics"));
                zipOut.write(calendarFile);
                zipOut.closeEntry();

                zipOut.putNextEntry(new ZipEntry("watering_plan.pdf"));
                zipOut.write(pdfFile);
                zipOut.closeEntry();
            }

            byte[] zipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("watering_files.zip").build());

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @PreAuthorize("hasAnyRole('DESIGNER_USER', 'SIMPLE_USER', 'ADMIN_USER')")
    @GetMapping("/download/pdf/{wateringPlanId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long wateringPlanId) {
        try {
            WateringPlan wateringPlan = wateringPlanService.getWateringPlanById(wateringPlanId);
            byte[] pdf = fileGeneratorService.generateWateringPlanPdf(wateringPlan);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("watering_plan.pdf").build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
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
            @RequestParam("img") MultipartFile img,
            HttpServletRequest request) {

        String imageUrl;
        try {
            imageUrl = s3Service.uploadFile("wateringDays", img);
        } catch (IOException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.name(),
                    "Error uploading img to S3",
                    HttpStatus.INTERNAL_SERVER_ERROR, request);
        }

        WateringDay updatedWateringDay = wateringPlanService.addImageToWateringDay(wateringDayId, imageUrl);
        return new GlobalHandlerResponse()
                .handleResponse(HttpStatus.OK.name(),
                        updatedWateringDay, HttpStatus.OK, request);
    }


    private ResponseEntity<?> getWateringPlansByUserId(Long userId, int page, int size, HttpServletRequest request) {
        Page<WateringPlanDTO> wateringPlanPage = wateringPlanService.getWateringPlansByUser(userId, page, size);
        return PaginationUtils.getPaginatedResponse(wateringPlanPage, request);
    }

}
