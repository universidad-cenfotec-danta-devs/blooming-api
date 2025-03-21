package com.blooming.api.service.nursery;

import com.blooming.api.entity.Nursery;
import com.blooming.api.repository.nursery.INurseryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NurseryService {

    @Autowired
    private INurseryRepository nurseryRepository;

    public Nursery createNursery(Nursery nursery) {
        return nurseryRepository.save(nursery);
    }

    public Nursery updateNursery(Long id, Nursery nursery) {
        if (nurseryRepository.existsById(id)){
            nursery.setId(id);
            return nurseryRepository.save(nursery);
        } else {
            return null;
        }
    }

    public Nursery approveNursery(Long id) {
        Nursery nursery = nurseryRepository.findById(id).orElse(null);
        if (nursery != null){
            nursery.setActive(true);
            return nurseryRepository.save(nursery);
        }
        return null;
    }

    public List<Nursery> getAllNurseries() {
        return nurseryRepository.findAll();
    }
}
