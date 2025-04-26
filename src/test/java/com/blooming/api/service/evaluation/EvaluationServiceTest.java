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

    private static final long POT_ID = 1L;
    private static final long NURSERY_ID = 1L;
    private static final long VALID_EVAL_ID = 10L;
    private static final long INVALID_EVAL_ID = 99L;
    private static final int POT_RATING = 4;
    private static final int NURSERY_RATING = 3;
    private static final int DEACTIVATE_SUCCESS = 1;
    private static final int DEACTIVATE_FAIL = 0;
    private static final String POT_COMMENT = "Nice pot";
    private static final String NURSERY_COMMENT = "test_review";
    private static final String INVALID_COMMENT = "Invalid";
    private static final String INVALID_ENTITY_TYPE = "invalid_type";

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
        EvaluationRequest request = new EvaluationRequest(POT_ID, POT_RATING, false, POT_COMMENT);
        Evaluation evaluation = new Evaluation(user, pot, null, POT_RATING, POT_COMMENT, false);
        evaluation.setCreatedAt(new Date());
        when(evaluationRepository.save(any())).thenReturn(evaluation);

        EvaluationDTO dto = evaluationService.createEvaluation(pot, request, user);

        System.out.println("Evaluación de maceta registrada: " + dto.toString());
        assertEquals(POT_RATING, dto.rating());
        assertEquals(POT_COMMENT, dto.comment());
    }

    @Test
    void testCreateEvaluation_ForNursery_Success() {
        User user = new User();
        Nursery nursery = new Nursery();
        EvaluationRequest request = new EvaluationRequest(NURSERY_ID, NURSERY_RATING, true, NURSERY_COMMENT);
        Evaluation evaluation = new Evaluation(user, null, nursery, NURSERY_RATING, NURSERY_COMMENT, true);
        evaluation.setCreatedAt(new Date());

        when(evaluationRepository.save(any())).thenReturn(evaluation);

        EvaluationDTO dto = evaluationService.createEvaluation(nursery, request, user);
        System.out.println("Evaluación de vivero registrada: " + dto.toString());

        assertEquals(NURSERY_RATING, dto.rating());
        assertEquals(NURSERY_COMMENT, dto.comment());
    }

    @Test
    void testCreateEvaluation_InvalidEntity_ThrowsException() {
        User user = new User();
        EvaluationRequest request = new EvaluationRequest(1L, 5, false, INVALID_COMMENT);

        assertThrows(IllegalArgumentException.class, () ->
                evaluationService.createEvaluation(INVALID_ENTITY_TYPE, request, user));
    }

    @Test
    void testGetEvaluationById_Success() {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(VALID_EVAL_ID);
        evaluation.setCreatedAt(new Date());
        when(evaluationRepository.findById(VALID_EVAL_ID)).thenReturn(Optional.of(evaluation));

        EvaluationDTO dto = evaluationService.getEvaluationById(VALID_EVAL_ID);
        assertEquals(VALID_EVAL_ID, dto.id());
    }

    @Test
    void testGetEvaluationById_NotFound_ThrowsException() {
        when(evaluationRepository.findById(INVALID_EVAL_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                evaluationService.getEvaluationById(INVALID_EVAL_ID));
    }

    @Test
    void testDeactivateEvaluation_Success() {
        when(evaluationRepository.existsById(POT_ID)).thenReturn(true);
        when(evaluationRepository.deactivate(POT_ID)).thenReturn(DEACTIVATE_SUCCESS);

        assertDoesNotThrow(() -> evaluationService.deactivate(POT_ID));
    }

    @Test
    void testDeactivateEvaluation_Failure() {
        when(evaluationRepository.existsById(POT_ID)).thenReturn(true);
        when(evaluationRepository.deactivate(POT_ID)).thenReturn(DEACTIVATE_FAIL);

        assertThrows(IllegalArgumentException.class, () ->
                evaluationService.deactivate(POT_ID));
    }
}
