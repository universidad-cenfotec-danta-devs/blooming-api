package com.blooming.api.service.nursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.request.UpdateNurseryRequest;
import com.blooming.api.response.dto.NurseryDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface INurseryService {
//    List<NurseryDTO> getAllNurseries();
    ResponseEntity<?> getAllNurseries(int page, int size, HttpServletRequest request);

    NurseryDTO getNurseryById(Long id);
    List<NurseryDTO> getMyNurseries(Long idNurseryAdmin);
    ResponseEntity<?> getProductsByNursery(Long idNursery, HttpServletRequest request);
    ResponseEntity<?> createNursery(Nursery nursery, HttpServletRequest request);
    NurseryDTO updateNursery(Long idNursery, UpdateNurseryRequest request);

}
