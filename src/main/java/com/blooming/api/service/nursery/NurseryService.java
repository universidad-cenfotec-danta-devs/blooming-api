package com.blooming.api.service.nursery;

import com.amazonaws.services.kms.model.NotFoundException;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.entity.User;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.request.NurseryRequest;
import com.blooming.api.request.NurseryUpdateRequest;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NurseryService implements INurseryService {

    private final INurseryRepository nurseryRepository;

    public NurseryService(INurseryRepository nurseryRepository) {
        this.nurseryRepository = nurseryRepository;
    }

    @Override
    public ResponseEntity<?> getAllNurseries(int page, int size, HttpServletRequest request){
        List<NurseryDTO> nurseryDTOS = new ArrayList<NurseryDTO>();
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Nursery> nurseryPage = nurseryRepository.findAll(pageable);

        for (Nursery nursery : nurseryPage.getContent()){
            NurseryDTO nurseryDTO = ParsingUtils.toNurseryDTO(nursery);
            nurseryDTOS.add(nurseryDTO);
        }

        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURI());
        meta.setTotalPages(nurseryPage.getTotalPages());
        meta.setTotalElements(nurseryPage.getTotalElements());
        meta.setPageNumber(nurseryPage.getNumber());
        meta.setPageSize(nurseryPage.getSize());

        return new GlobalHandlerResponse().handleResponse("Nurseries retrieve successfully", nurseryDTOS, HttpStatus.OK, meta);
    }


    @Override
    public NurseryDTO createNursery(NurseryRequest nurseryRequest, User user, String imgUrl) {
        Nursery nursery = new Nursery();
        nursery.setNurseryAdmin(user);
        nursery.setName(nurseryRequest.name());
        nursery.setDescription(nurseryRequest.description());
        nursery.setLatitude(nurseryRequest.latitude());
        nursery.setLongitude(nurseryRequest.longitude());
        nursery.setImageUrl(imgUrl);
        return ParsingUtils.toNurseryDTO(nurseryRepository.save(nursery));
    }

    @Override
    public Page<NurseryDTO> getAllNurseries(int page, int size, boolean status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Nursery> nurseryPage = nurseryRepository.findNurseriesByStatus(status, pageable);
        return nurseryPage.map(ParsingUtils::toNurseryDTO);
    }

    @Override
    public Nursery getNurseryByNurseryAdminId(Long id) {
        return nurseryRepository.findByNurseryAdminId(id)
                .orElseThrow(() -> new EntityNotFoundException("Nursery Admin with id " + id + " not found"));
    }

    @Override
    public NurseryDTO getNurseryDTOById(Long id) {
        Nursery nursery = getNurseryById(id);
        return ParsingUtils.toNurseryDTO(nursery);
    }

    @Override
    public Nursery getNurseryById(Long id) {
        return nurseryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Nursery with id " + id + " not found"));
    }

    @Override
    public NurseryDTO updateNursery(Long idNursery, NurseryUpdateRequest request) {

        Nursery currentNursery = getNurseryById(idNursery);

        if (request.name() != null) {
            currentNursery.setName(request.name());
        }
        if (request.description() != null) {
            currentNursery.setDescription(request.description());
        }

        if (request.latitude() != currentNursery.getLatitude()) {
            currentNursery.setLatitude(request.latitude());
        }

        if (request.longitude() != currentNursery.getLongitude()) {
            currentNursery.setLongitude(request.longitude());
        }
        Nursery nursery = nurseryRepository.save(currentNursery);
        return ParsingUtils.toNurseryDTO(nursery);
    }

    @Override
    @Transactional
    public void activate(Long nurseryId) {
        if (!nurseryRepository.existsById(nurseryId)) {
            throw new EntityNotFoundException("Nursery not found with id: " + nurseryId);
        }
        int updatedRows = nurseryRepository.activate(nurseryId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to activate nursery with id: " + nurseryId);
        }
    }

    @Override
    @Transactional
    public void deactivate(Long nurseryId) {
        if (!nurseryRepository.existsById(nurseryId)) {
            throw new EntityNotFoundException("Nursery not found with id: " + nurseryId);
        }
        int updatedRows = nurseryRepository.deactivate(nurseryId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to deactivate nursery with id: " + nurseryId);
        }
    }


    @Override
    public NurseryDTO addProductToNursery(Long nurseryAdminId, ProductRequest productRequest) {
        Nursery nursery = getNurseryByNurseryAdminId(nurseryAdminId);

        List<Product> products = nursery.getProducts();
        Product product = new Product();
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setNursery(nursery);
        products.add(product);
        nursery.setProducts(products);

        nurseryRepository.save(nursery);
        return ParsingUtils.toNurseryDTO(nursery);
    }

    @Override
    public Page<NurseryDTO> findNearby(int page, int size, double latitude, double longitude, double radius) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List <NurseryDTO> nurseryDTOS = nurseryRepository.findNurseriesByStatus(true, pageable).stream().filter(nursery -> {
            double distance = calculateDistance(latitude, longitude, nursery.getLatitude(), nursery.getLongitude()
            );
            return distance <= radius;
        })
        .map(ParsingUtils::toNurseryDTO).toList();
        return new PageImpl<>(nurseryDTOS, pageable, nurseryDTOS.size());
    }

    @Override
    public NurseryDTO addProductToNurseryAsAdmin(Long idNursery, ProductRequest product) {
        Product newProduct = new Product();
        newProduct.setName(product.name());
        newProduct.setDescription(product.description());
        newProduct.setPrice(product.price());
        Nursery nursery = nurseryRepository.findById(idNursery).orElseThrow(() ->
                new NotFoundException("Nursery not found"));
        newProduct.setNursery(nursery);
        nursery.getProducts().add(newProduct);
        return ParsingUtils.toNurseryDTO(nurseryRepository.save(nursery));
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
