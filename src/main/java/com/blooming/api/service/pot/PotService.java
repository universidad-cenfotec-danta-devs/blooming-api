package com.blooming.api.service.pot;

import com.blooming.api.entity.Pot;
import com.blooming.api.repository.pot.IPotRepository;
import com.blooming.api.response.dto.PotDTO;
import com.blooming.api.utils.ParsingUtils;
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
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<PotDTO> getAllPots(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pot> plantsPage = potRepository.findByStatus(true, pageable);
        return plantsPage.map(ParsingUtils::toPotDTO);
    }
}
