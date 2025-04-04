package com.blooming.api.controller;


import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.openAI.IOpenAIService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openAI")
public class OpenAIController {

    private final IOpenAIService openAIService;

    public OpenAIController(IOpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    @GetMapping("/byLocation/{canton}")
    public ResponseEntity<?> getPlantsByLocation(
            @PathVariable("canton") String canton,
            HttpServletRequest request) {
        try {
            String result = openAIService.getFaunaByLocation(canton);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    result,
                    HttpStatus.OK, request);
        } catch (RuntimeException e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.name(),
                    "Error processing request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

}
