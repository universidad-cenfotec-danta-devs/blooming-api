package com.blooming.api.service.evaluation;

import com.blooming.api.entity.Evaluation;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.repository.evaluation.IEvaluationRepository;
import com.blooming.api.request.EvaluationRequest;
import com.blooming.api.response.dto.EvaluationDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluationServiceTest {

    @InjectMocks
    private EvaluationService evaluationService;

    @Mock
    private IEvaluationRepository evaluationRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testCreateEvaluation_ForPot_Success() {
        User user = new User();
        Pot pot = new Pot();
        EvaluationRequest request = new EvaluationRequest(1L, 4, false, "Nice pot");
        Evaluation evaluation = new Evaluation(user, pot, null, 4, "Nice pot", false);
        evaluation.setCreatedAt(new Date());
        when(evaluationRepository.save(any())).thenReturn(evaluation);

        EvaluationDTO dto = evaluationService.createEvaluation(pot, request, user);

        assertEquals(4, dto.rating());
        assertEquals("Nice pot", dto.comment());
    }

    @Test
    void testCreateEvaluation_ForNursery_Success() {
        User user = new User();
        Nursery nursery = new Nursery();
        EvaluationRequest request = new EvaluationRequest(1L, 3, true, "test_review");
        Evaluation evaluation = new Evaluation(user, null, nursery, 3, "test_review", true);
        evaluation.setCreatedAt(new Date());

        when(evaluationRepository.save(any())).thenReturn(evaluation);

        EvaluationDTO dto = evaluationService.createEvaluation(nursery, request, user);

        assertEquals(3, dto.rating());
        assertEquals("test_review", dto.comment());
    }

    @Test
    void testCreateEvaluation_InvalidEntity_ThrowsException() {
        User user = new User();
        EvaluationRequest request = new EvaluationRequest(1L, 5, false, "Invalid");

        assertThrows(IllegalArgumentException.class, () ->
                evaluationService.createEvaluation("invalid_type", request, user));
    }

    @Test
    void testGetEvaluationById_Success() {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(10L);
        evaluation.setCreatedAt(new Date());
        when(evaluationRepository.findById(10L)).thenReturn(Optional.of(evaluation));

        EvaluationDTO dto = evaluationService.getEvaluationById(10L);
        assertEquals(10L, dto.id());
    }

    @Test
    void testGetEvaluationById_NotFound_ThrowsException() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                evaluationService.getEvaluationById(99L));
    }

    @Test
    void testDeactivateEvaluation_Success() {
        when(evaluationRepository.existsById(1L)).thenReturn(true);
        when(evaluationRepository.deactivate(1L)).thenReturn(1);

        assertDoesNotThrow(() -> evaluationService.deactivate(1L));
    }

    @Test
    void testDeactivateEvaluation_Failure() {
        when(evaluationRepository.existsById(1L)).thenReturn(true);
        when(evaluationRepository.deactivate(1L)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () ->
                evaluationService.deactivate(1L));
    }
}
