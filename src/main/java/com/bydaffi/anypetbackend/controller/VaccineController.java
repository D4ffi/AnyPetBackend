package com.bydaffi.anypetbackend.controller;

import com.bydaffi.anypetbackend.models.PetType;
import com.bydaffi.anypetbackend.models.Vaccine;
import com.bydaffi.anypetbackend.service.VaccineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for vaccine management endpoints
 */
@RestController
@RequestMapping("/api/vaccines")
public class VaccineController {

    private final VaccineService vaccineService;

    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    /**
     * GET /api/vaccines - Get all vaccines
     */
    @GetMapping
    public ResponseEntity<List<Vaccine>> getAllVaccines() {
        return ResponseEntity.ok(vaccineService.getAllVaccines());
    }

    /**
     * GET /api/vaccines/{id} - Get vaccine by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vaccine> getVaccineById(@PathVariable Long id) {
        return vaccineService.getVaccineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/vaccines/pet-type/{petType} - Get vaccines for specific pet type
     * Example: /api/vaccines/pet-type/DOG
     */
    @GetMapping("/pet-type/{petType}")
    public ResponseEntity<List<Vaccine>> getVaccinesByPetType(@PathVariable String petType) {
        try {
            PetType type = PetType.valueOf(petType.toUpperCase());
            return ResponseEntity.ok(vaccineService.getVaccinesForPetType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/vaccines/pet-type/{petType}/core - Get core vaccines for specific pet type
     * Example: /api/vaccines/pet-type/CAT/core
     */
    @GetMapping("/pet-type/{petType}/core")
    public ResponseEntity<List<Vaccine>> getCoreVaccinesByPetType(@PathVariable String petType) {
        try {
            PetType type = PetType.valueOf(petType.toUpperCase());
            return ResponseEntity.ok(vaccineService.getCoreVaccinesForPetType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/vaccines/core - Get all core vaccines
     */
    @GetMapping("/core")
    public ResponseEntity<List<Vaccine>> getAllCoreVaccines() {
        return ResponseEntity.ok(vaccineService.getAllCoreVaccines());
    }

    /**
     * GET /api/vaccines/optional - Get all optional vaccines
     */
    @GetMapping("/optional")
    public ResponseEntity<List<Vaccine>> getAllOptionalVaccines() {
        return ResponseEntity.ok(vaccineService.getAllOptionalVaccines());
    }

    /**
     * GET /api/vaccines/statistics/{petType} - Get vaccine statistics for a pet type
     */
    @GetMapping("/statistics/{petType}")
    public ResponseEntity<VaccineService.VaccineStatistics> getStatistics(@PathVariable String petType) {
        try {
            PetType type = PetType.valueOf(petType.toUpperCase());
            return ResponseEntity.ok(vaccineService.getStatisticsForPetType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/vaccines - Create a new vaccine
     */
    @PostMapping
    public ResponseEntity<Vaccine> createVaccine(@RequestBody Vaccine vaccine) {
        Vaccine created = vaccineService.createVaccine(vaccine);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/vaccines/{id} - Update an existing vaccine
     */
    @PutMapping("/{id}")
    public ResponseEntity<Vaccine> updateVaccine(@PathVariable Long id, @RequestBody Vaccine vaccine) {
        try {
            Vaccine updated = vaccineService.updateVaccine(id, vaccine);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/vaccines/{id} - Delete a vaccine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVaccine(@PathVariable Long id) {
        vaccineService.deleteVaccine(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/vaccines/count - Get total number of vaccines
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countVaccines() {
        return ResponseEntity.ok(vaccineService.countVaccines());
    }
}
