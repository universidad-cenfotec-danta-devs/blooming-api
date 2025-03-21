package com.blooming.api.controller;

import com.blooming.api.entity.Nursery;
import com.blooming.api.service.nursery.NurseryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nurseries")
public class NurseryController {

    @Autowired
    private NurseryService nurseryService;

    @PostMapping
    public ResponseEntity<Nursery> createNursery(@RequestBody Nursery nursery) {
        Nursery createNursery = nurseryService.createNursery(nursery);
        return ResponseEntity.ok(createNursery);
    }

    @GetMapping
    public ResponseEntity<List<Nursery>> getAllNurseries() {
        List<Nursery> nurseries = nurseryService.getAllNurseries();
        return ResponseEntity.ok(nurseries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nursery> updateNursery(@PathVariable Long id, @RequestBody Nursery nursery) {
        Nursery updateNursery = nurseryService.updateNursery(id, nursery);
        return ResponseEntity.ok(updateNursery);
    }
}
