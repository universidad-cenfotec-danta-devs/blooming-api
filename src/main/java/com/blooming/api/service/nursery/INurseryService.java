package com.blooming.api.service.nursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.request.NurseryRequest;
import com.blooming.api.request.NurseryUpdateRequest;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.response.dto.NurseryDTO;
import org.springframework.data.domain.Page;

public interface INurseryService {
    NurseryDTO createNursery(NurseryRequest nursery, String imgUrl);
    Page<NurseryDTO> getAllNurseries(int page, int size, boolean status);
    Nursery getNurseryByNurseryAdminId(Long id);
    Nursery getNurseryById(Long id);
    NurseryDTO getNurseryDTOById(Long id);
    NurseryDTO updateNursery(Long idNursery, NurseryUpdateRequest nurseryRequest);
    void activate(Long nurseryId);
    void deactivate(Long nurseryId);
    NurseryDTO addProductToNursery(Long nurseryAdminId, ProductRequest productRequest);
}