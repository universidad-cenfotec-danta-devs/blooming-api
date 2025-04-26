package com.blooming.api.service.pot;

import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import com.blooming.api.response.dto.PotDTO;
import org.springframework.data.domain.Page;

public interface IPotService {
    PotDTO register(Pot pot);

    Page<PotDTO> getAllPots(int page, int size);

    Page<PotDTO> getPotsByDesigner(User designer, boolean status, int page, int size);

    Pot getPotById(Long id);

    void activate(Long potId);

    void deactivate(Long potId);
}
