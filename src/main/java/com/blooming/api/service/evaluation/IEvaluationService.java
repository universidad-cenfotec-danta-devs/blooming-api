package com.blooming.api.service.evaluation;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.request.EvaluationRequest;
import com.blooming.api.response.dto.EvaluationDTO;
import org.springframework.data.domain.Page;

public interface IEvaluationService {
    <T> EvaluationDTO createEvaluation(T evaluatedEntity, EvaluationRequest evaluationRequest, User user);

    EvaluationDTO getEvaluationById(Long id);

    Page<EvaluationDTO> getAllEvaluationsByPotAndStatus(Pot pot, boolean status, int page, int size);

    Page<EvaluationDTO> getAllEvaluationsByNurseryAndStatus(Nursery nursery, boolean status, int page, int size);

    void activate(Long evaluationId);

    void deactivate(Long evaluationId);
}
