package com.blooming.api.service.pot;

import com.blooming.api.entity.Pot;
import com.blooming.api.repository.pot.IPotRepository;
import com.blooming.api.response.dto.PotDTO;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PotService implements IPotService {
    private final IPotRepository potRepository;

    public PotService(IPotRepository potRepository) {
        this.potRepository = potRepository;
    }

    @Override
    public PotDTO register(Pot pot) {
        try {
            Pot save = potRepository.save(pot);
            return ParsingUtils.toPotDTO(save);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error al guardar la maceta: los datos proporcionados no son válidos.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al registrar la maceta.", e);
        }
    }


    @Override
    public Page<PotDTO> getAllPots(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pot> plantsPage = potRepository.findByStatus(true, pageable);
        return plantsPage.map(ParsingUtils::toPotDTO);
    }

    @Override
    public Pot getPotById(Long id) {
        return potRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pot with id " + id + " not found"));
    }

    @Override
    @Transactional
    public void activate(Long potId) {
        if (!potRepository.existsById(potId)) {
            throw new EntityNotFoundException("Pot not found with id: " + potId);
        }
        int updatedRows = potRepository.activate(potId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to activate pot with id: " + potId);
        }
    }

    @Override
    @Transactional
    public void deactivate(Long potId) {
        if (!potRepository.existsById(potId)) {
            throw new EntityNotFoundException("Pot not found with id: " + potId);
        }
        int updatedRows = potRepository.deactivate(potId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to deactivate pot with id: " + potId);
        }
    }
}
