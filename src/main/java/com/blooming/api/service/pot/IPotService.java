package com.blooming.api.service.pot;

import com.blooming.api.entity.Pot;
import com.blooming.api.response.dto.PotDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface IPotService {
    PotDTO register(Pot pot);

    Page<PotDTO> getAllPots(int page, int size);

    Pot getPotById(Long id);

}
