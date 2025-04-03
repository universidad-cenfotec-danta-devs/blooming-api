package com.blooming.api.controller;

import com.blooming.api.entity.Nursery;
import com.blooming.api.request.UpdateNurseryRequest;
import com.blooming.api.response.dto.NurseryDTO;
import com.blooming.api.service.nursery.INurseryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nurseries")
public class NurseryController {

    @Autowired
    private INurseryService nurseryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER')")

    public ResponseEntity<?> getAllNurseries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request
    ){
        return nurseryService.getAllNurseries(page,size,request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'SIMPLE_USER')")
    public ResponseEntity<NurseryDTO> getNurseryById(@PathVariable Long id){
        NurseryDTO nursery = nurseryService.getNurseryById(id);
        return ResponseEntity.ok(nursery);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'SIMPLE_USER')")
    public ResponseEntity<?> createNursery(@RequestBody Nursery nursery, HttpServletRequest request){
        return nurseryService.createNursery(nursery, request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'SIMPLE_USER')")
    public ResponseEntity<NurseryDTO> updateNursery(@PathVariable Long id, @RequestBody UpdateNurseryRequest nurseryRequest){
        NurseryDTO updatedNursery = nurseryService.updateNursery(id, nurseryRequest);
        return ResponseEntity.ok(updatedNursery);
    }

//    Mover al controlador de usuario
    @GetMapping("/{idNurseryAdmin}/myNurseries")
    @PreAuthorize("hasAnyRole('SIMPLE_USER')")
    public ResponseEntity<List<NurseryDTO>> myNurseries(@PathVariable Long idNurseryAdmin){
        List<NurseryDTO> allMyNurseries = nurseryService.getMyNurseries(idNurseryAdmin);
        return ResponseEntity.ok(allMyNurseries);
    }
}
