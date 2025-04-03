package com.blooming.api.service.nursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Product;
import com.blooming.api.repository.nursery.INurseryRepository;
import com.blooming.api.request.NurseryRequest;
import com.blooming.api.request.NurseryUpdateRequest;
import com.blooming.api.request.ProductRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.utils.ParsingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NurseryService implements INurseryService {

    private final INurseryRepository nurseryRepository;

    public NurseryService(INurseryRepository nurseryRepository) {
        this.nurseryRepository = nurseryRepository;
    }

    @Override
    public NurseryDTO createNursery(NurseryRequest nurseryRequest, String imgUrl) {
        Nursery nursery = new Nursery();
        nursery.setName(nurseryRequest.name());
        nursery.setDescription(nurseryRequest.description());
        nursery.setLatitude(nurseryRequest.latitude());
        nursery.setLongitude(nurseryRequest.longitude());
        nursery.setImageUrl(imgUrl);
        return ParsingUtils.toNurseryDTO(nurseryRepository.save(nursery));
    }

    @Override
    public Page<NurseryDTO> getAllNurseries(int page, int size, boolean status) {
        Pageable pageable = PageRequest.of(page, size);
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
        products.add(product);
        nursery.setProducts(products);

        nurseryRepository.save(nursery);
        return ParsingUtils.toNurseryDTO(nursery);
    }

}
