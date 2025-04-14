package com.blooming.api.controller;

import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.request.PotRequest;
import com.blooming.api.response.dto.PotDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.pot.IPotService;
import com.blooming.api.service.s3.IS3Service;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.utils.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/pot")
public class PotController {

    private final IPotService potService;
    private final IUserService userService;
    private final IS3Service s3Service;
    private final JwtService jwtService;


    public PotController(IPotService potService, IUserService userService, IS3Service s3Service, JwtService jwtService) {
        this.potService = potService;
        this.userService = userService;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPot(
            @RequestPart("potRequest") @Valid PotRequest potRequest,
            @RequestPart("3dFile") MultipartFile pot3dFile,
            HttpServletRequest request) {

        try {
            String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
            User user = userService.findByEmail(userEmail).
                    orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
            String potUrl;
            try {
                potUrl = s3Service.uploadFile("pot", pot3dFile);
            } catch (IOException e) {
                return new GlobalHandlerResponse().handleResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        "Error uploading 3d file to S3",
                        HttpStatus.INTERNAL_SERVER_ERROR, request);
            }
            Pot pot = new Pot();
            pot.setName(potRequest.name());
            pot.setDescription(potRequest.description());
            pot.setImageUrl(potUrl);
            pot.setDesigner(user);
            pot.setPrice(potRequest.price());

            PotDTO dto = potService.register(pot);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    dto,
                    HttpStatus.OK, request);

        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    e.getMessage(),
                    HttpStatus.OK, request);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER')")
    @GetMapping
    public ResponseEntity<?> getPots(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     HttpServletRequest request) {
        try {
            Page<PotDTO> potPage = potService.getAllPots(page, size);
            return PaginationUtils.getPaginatedResponse(potPage, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

}
