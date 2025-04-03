package com.blooming.api.service.nursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.entity.ProductNursery;
import com.blooming.api.entity.User;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.repository.user.IUserRepository;
import com.blooming.api.request.UpdateNurseryRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NurseryService implements INurseryService{

    @Autowired
    private INurseryRepository nurseryRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public ResponseEntity<?> getAllNurseries(int page, int size, HttpServletRequest request){
        List<NurseryDTO> nurseryDTOS = new ArrayList<NurseryDTO>();
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Nursery> nurseryPage = nurseryRepository.findAll(pageable);

        for (Nursery nursery : nurseryPage.getContent()){
            NurseryDTO nurseryDTO = new NurseryDTO();
            nurseryDTO.setId(nursery.getId());
            nurseryDTO.setName(nursery.getName());
            nurseryDTO.setLongitude(nursery.getLongitude());
            nurseryDTO.setLatitude(nursery.getLatitude());
            nurseryDTO.setActive(nursery.isActive());
            nurseryDTOS.add(nurseryDTO);
        }

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI().toString());
        meta.setTotalPages(nurseryPage.getTotalPages());
        meta.setTotalElements(nurseryPage.getTotalElements());
        meta.setPageNumber(nurseryPage.getNumber());
        meta.setPageSize(nurseryPage.getSize());

        return new GlobalHandlerResponse().handleResponse("Nurseries retrieve successfully", nurseryDTOS, HttpStatus.OK, meta);
    }

    @Override
    public NurseryDTO getNurseryById(Long id) {
        NurseryDTO nurseryDTO = new NurseryDTO();
        Optional<Nursery> nursery = nurseryRepository.findById(id);

        if (nursery.isPresent()){
            nurseryDTO.setId(nursery.get().getId());
            nurseryDTO.setName(nursery.get().getName());
            nurseryDTO.setLongitude(nursery.get().getLongitude());
            nurseryDTO.setLatitude(nursery.get().getLatitude());
            nurseryDTO.setActive(nursery.get().isActive());
        }
        return nurseryDTO;
    }

    @Override
    public List<NurseryDTO> getMyNurseries(Long idNurseryAdmin) {
        List<NurseryDTO> nurseryDTOS = new ArrayList<NurseryDTO>();
        Optional<User> adminNursery = userRepository.findById(idNurseryAdmin);
        if (adminNursery.isPresent()){
            for (Nursery nursery: adminNursery.get().getNurseriesList()){
                NurseryDTO nurseryDTO = new NurseryDTO();
                nurseryDTO.setId(nursery.getId());
                nurseryDTO.setName(nursery.getName());
                nurseryDTO.setLongitude(nursery.getLongitude());
                nurseryDTO.setLatitude(nursery.getLatitude());
                nurseryDTO.setActive(nursery.isActive());
                nurseryDTOS.add(nurseryDTO);
            }
        }
        return nurseryDTOS;
    }

//    Para la tabla intermedia
    @Override
    public ResponseEntity<?> getProductsByNursery(Long idNursery, HttpServletRequest request) {
        Optional<Nursery> nursery = nurseryRepository.findById(idNursery);
        List<Product> nurseryProducts = new ArrayList<>();

        if (nursery.isPresent()) {
            for(ProductNursery pn : nursery.get().getProductNurseryList()){
                nurseryProducts.add(pn.getProduct());
            }
        }
        return new GlobalHandlerResponse().handleResponse("Products by nursery retrieved successfully", nurseryProducts, HttpStatus.OK, request);
    }

    @Override
    public ResponseEntity<?> createNursery(Nursery nursery, HttpServletRequest request) {
        Nursery createdNursery = nurseryRepository.save(nursery);
        NurseryDTO nurseryDTO = new NurseryDTO();

        nurseryDTO.setId(createdNursery.getId());
        nurseryDTO.setName(createdNursery.getName());
        nurseryDTO.setLongitude(createdNursery.getLongitude());
        nurseryDTO.setLatitude(createdNursery.getLatitude());
        nurseryDTO.setActive(createdNursery.isActive());

        return new GlobalHandlerResponse().handleResponse("Nursery created successfully", nurseryDTO, HttpStatus.OK, request);
    }

    @Override
    public NurseryDTO updateNursery(Long idNursery, UpdateNurseryRequest request) {
        NurseryDTO nurseryDTO = new NurseryDTO();
        Optional<Nursery> currentOptionalNursery = nurseryRepository.findById(idNursery);

        if (currentOptionalNursery.isPresent()){
            Nursery currentNursery = currentOptionalNursery.get();

            if (request.getName() != null ){
                currentNursery.setName(request.getName());
            }

            if (request.getLatitude() != null){
                currentNursery.setLatitude(request.getLatitude());
            }

            if (request.getLongitude() != null){
                currentNursery.setLongitude(request.getLongitude());
            }

            if (request.getIsActive() != null){
                currentNursery.setActive(request.getIsActive());
            }

            nurseryRepository.save(currentNursery);

            nurseryDTO.setName(currentNursery.getName());
            nurseryDTO.setLatitude(currentNursery.getLatitude());
            nurseryDTO.setLongitude(currentNursery.getLongitude());
            nurseryDTO.setActive(currentNursery.isActive());
        }

        return nurseryDTO;
    }
}
