package com.blooming.api.controller;

import com.blooming.api.entity.EntityStatus;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.request.EvaluationRequest;
import com.blooming.api.response.dto.EvaluationDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.evaluation.IEvaluationService;
import com.blooming.api.service.nursery.INurseryService;
import com.blooming.api.service.pot.IPotService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import com.blooming.api.utils.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final IEvaluationService evaluationService;
    private final IPotService potService;
    private final INurseryService nurseryService;
    private final IUserService userService;
    private final JwtService jwtService;

    public EvaluationController(IEvaluationService evaluationService,
                                IPotService potService,
                                INurseryService nurseryService,
                                IUserService userService, JwtService jwtService1) {
        this.evaluationService = evaluationService;
        this.potService = potService;
        this.nurseryService = nurseryService;
        this.userService = userService;
        this.jwtService = jwtService1;
    }

    @PostMapping("/forPot")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> createEvaluationForPot(@Valid @RequestBody EvaluationRequest evaluationRequest,
                                                    HttpServletRequest request) {

        try {
            String emailToUse = getEmailToUse(evaluationRequest);
            User user = userService.findByEmail(emailToUse).
                    orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailToUse));

            Pot pot = potService.getPotById(evaluationRequest.objToEvaluateId());
            EvaluationDTO evaluation = evaluationService.createEvaluation(pot, evaluationRequest, user);

            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    evaluation,
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }


    @PostMapping("/forNursery")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> createEvaluationForNursery(@Valid @RequestBody EvaluationRequest evaluationRequest,
                                                        HttpServletRequest request) {

        try {
            String emailToUse = getEmailToUse(evaluationRequest);
            User user = userService.findByEmail(emailToUse).
                    orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailToUse));

            Nursery nursery = nurseryService.getNurseryById(evaluationRequest.objToEvaluateId());
            EvaluationDTO evaluation = evaluationService.createEvaluation(nursery, evaluationRequest, user);

            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    evaluation,
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }

    }

    @GetMapping("/{evaluationId}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getEvaluationById(@PathVariable Long evaluationId, HttpServletRequest request) {
        try {
            EvaluationDTO evaluation = evaluationService.getEvaluationById(evaluationId);
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.OK.name(),
                    evaluation,
                    HttpStatus.OK, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/nursery/{nurseryId}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getAllEvaluationsByNurseryAndStatus(@PathVariable Long nurseryId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "true") boolean status,
                                                                 HttpServletRequest request) {
        try {
            Nursery nursery = nurseryService.getNurseryById(nurseryId);
            Page<EvaluationDTO> evaluations = evaluationService.getAllEvaluationsByNurseryAndStatus(nursery, status, page, size);
            return PaginationUtils.getPaginatedResponse(evaluations, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/pot/{potId}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getAllEvaluationsByPotAndStatus(@PathVariable Long potId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "true") boolean status,
                                                             HttpServletRequest request) {
        try {
            Pot pot = potService.getPotById(potId);
            Page<EvaluationDTO> evaluations = evaluationService.getAllEvaluationsByPotAndStatus(pot, status, page, size);
            return PaginationUtils.getPaginatedResponse(evaluations, request);
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @GetMapping("/byUser")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> getAllEvaluationsByUser(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "true") boolean status,
                                                     HttpServletRequest request) {
        try {
            String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
            User user = userService.findByEmail(userEmail).
                    orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

            Page<EvaluationDTO> evaluations = evaluationService.getAllEvaluationsByUser(user, status, page, size);
            return PaginationUtils.getPaginatedResponse(evaluations, request);

        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PreAuthorize("hasRole('ADMIN_USER')")
    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activateEvaluation(@PathVariable("id") Long evaluationId, HttpServletRequest request) {
        return changeEvaluationStatus(evaluationId, EntityStatus.ACTIVATE, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER')")
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateEvaluation(@PathVariable("id") Long evaluationId, HttpServletRequest request) {
        return changeEvaluationStatus(evaluationId, EntityStatus.DEACTIVATE, request);
    }

    private ResponseEntity<?> changeEvaluationStatus(Long evaluationId, EntityStatus status, HttpServletRequest request) {
        try {
            return switch (status) {
                case ACTIVATE -> {
                    evaluationService.activate(evaluationId);
                    yield new GlobalHandlerResponse().handleResponse(
                            HttpStatus.OK.name(),
                            "Evaluation activated successfully",
                            HttpStatus.OK, request);
                }
                case DEACTIVATE -> {
                    evaluationService.deactivate(evaluationId);
                    yield new GlobalHandlerResponse().handleResponse(
                            HttpStatus.OK.name(),
                            "Evaluation deactivated successfully",
                            HttpStatus.OK, request);
                }
            };
        } catch (Exception e) {
            return new GlobalHandlerResponse().handleResponse(
                    HttpStatus.BAD_REQUEST.name(),
                    HttpStatus.BAD_REQUEST, request);
        }

    }

    private String getEmailToUse(EvaluationRequest evaluationRequest) {
        return (evaluationRequest.userEmail() != null && !evaluationRequest.userEmail().isEmpty())
                ? evaluationRequest.userEmail()
                : "unknown_user@gmail.com";
    }
}
