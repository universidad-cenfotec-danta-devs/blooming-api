package com.blooming.api.controller;

import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.request.EvaluationRequest;
import com.blooming.api.response.dto.EvaluationDTO;
import com.blooming.api.service.evaluation.IEvaluationService;
import com.blooming.api.service.pot.IPotService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    private static final Long POT_ID = 1L;
    private static final int RATING = 5;
    private static final boolean IS_PUBLIC = true;
    private static final String COMMENT = "test_comment";
    private static final String TOKEN_HEADER = "Bearer token123";
    private static final String TOKEN = "token123";
    private static final String EMAIL = "user@email.com";
    private static final String EVALUATION_COMMENT = "great";
    private static final String USERNAME = "Enzo";
    private static final String CREATED_AT = "2025-04-21T15:30:00";
    private static final String REQUEST_URL = "www.blooming.com";

    @InjectMocks
    private EvaluationController evaluationController;

    @Mock
    private IEvaluationService evaluationService;
    @Mock
    private IPotService potService;
    @Mock
    private IUserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));
    }

    @Test
    void testCreateEvaluationForPot() {
        EvaluationRequest req = new EvaluationRequest(POT_ID, RATING, IS_PUBLIC, COMMENT);
        Pot pot = new Pot();
        User user = new User();

        when(request.getHeader("Authorization")).thenReturn(TOKEN_HEADER);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(potService.getPotById(POT_ID)).thenReturn(pot);
        when(evaluationService.createEvaluation(pot, req, user)).thenReturn(getMockEvaluation());

        ResponseEntity<?> response = evaluationController.createEvaluationForPot(req, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetEvaluationById() {
        when(evaluationService.getEvaluationById(POT_ID)).thenReturn(getMockEvaluation());
        ResponseEntity<?> response = evaluationController.getEvaluationById(POT_ID, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeactivateEvaluation() {
        doNothing().when(evaluationService).deactivate(POT_ID);
        ResponseEntity<?> response = evaluationController.deactivateEvaluation(POT_ID, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private EvaluationDTO getMockEvaluation() {
        return new EvaluationDTO(
                POT_ID,
                RATING,
                EVALUATION_COMMENT,
                CREATED_AT,
                USERNAME
        );
    }

}
