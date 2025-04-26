package com.blooming.api.service.evaluation;

import com.blooming.api.entity.Evaluation;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.repository.evaluation.IEvaluationRepository;
import com.blooming.api.request.EvaluationRequest;
import com.blooming.api.response.dto.EvaluationDTO;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService implements IEvaluationService {
    private final IEvaluationRepository evaluationRepository;

    public EvaluationService(IEvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    @Transactional
    public <T> EvaluationDTO createEvaluation(T evaluatedEntity,
                                              EvaluationRequest evaluationRequest,
                                              User user) {
        Evaluation evaluation;

        if (evaluatedEntity instanceof Pot pot) {
            evaluation = new Evaluation(user, pot, null, evaluationRequest.rating(), evaluationRequest.comment(), evaluationRequest.anonymous());
        } else if (evaluatedEntity instanceof Nursery nursery) {
            evaluation = new Evaluation(user, null, nursery, evaluationRequest.rating(), evaluationRequest.comment(), evaluationRequest.anonymous());
        } else {
            throw new IllegalArgumentException("Invalid evaluated entity type");
        }

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return ParsingUtils.toEvaluationDTO(savedEvaluation);
    }

    @Override
    public EvaluationDTO getEvaluationById(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with id " + id));
        return ParsingUtils.toEvaluationDTO(evaluation);
    }

    @Override
    public Page<EvaluationDTO> getAllEvaluationsByNurseryAndStatus(Nursery nursery, boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Evaluation> evaluations = evaluationRepository.findByNurseryAndStatus(nursery, status, pageable);
        return evaluations.map(ParsingUtils::toEvaluationDTO);
    }

    @Override
    public Page<EvaluationDTO> getAllEvaluationsByUser(User user, boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Evaluation> evaluations = evaluationRepository.findByUserAndStatus(user, status, pageable);
        return evaluations.map(ParsingUtils::toEvaluationDTO);
    }

    @Override
    public Page<EvaluationDTO> getAllEvaluationsByPotAndStatus(Pot pot, boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Evaluation> evaluations = evaluationRepository.findByPotAndStatus(pot, status, pageable);
        return evaluations.map(ParsingUtils::toEvaluationDTO);
    }

    @Override
    @Transactional
    public void activate(Long evaluationId) {
        if (!evaluationRepository.existsById(evaluationId)) {
            throw new EntityNotFoundException("Evaluation not found with id: " + evaluationId);
        }
        int updatedRows = evaluationRepository.activate(evaluationId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to activate evaluation with id: " + evaluationId);
        }
    }

    @Override
    @Transactional
    public void deactivate(Long evaluationId) {
        if (!evaluationRepository.existsById(evaluationId)) {
            throw new EntityNotFoundException("Evaluation not found with id: " + evaluationId);
        }
        int updatedRows = evaluationRepository.deactivate(evaluationId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to deactivate evaluation with id: " + evaluationId);
        }
    }


}
