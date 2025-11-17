package com.bydaffi.anypetbackend.service;

import com.bydaffi.anypetbackend.models.PetType;
import com.bydaffi.anypetbackend.models.Vaccine;
import com.bydaffi.anypetbackend.repository.VaccineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing vaccine operations
 */
@Service
public class VaccineService {

    private final VaccineRepository vaccineRepository;

    public VaccineService(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    /**
     * Get all vaccines in the system
     */
    public List<Vaccine> getAllVaccines() {
        return vaccineRepository.findAll();
    }

    /**
     * Get vaccine by ID
     */
    public Optional<Vaccine> getVaccineById(Long id) {
        return vaccineRepository.findById(id);
    }

    /**
     * Get all vaccines for a specific pet type
     */
    public List<Vaccine> getVaccinesForPetType(PetType petType) {
        return vaccineRepository.findByTargetSpecies(petType.name());
    }

    /**
     * Get only core (essential) vaccines for a pet type
     */
    public List<Vaccine> getCoreVaccinesForPetType(PetType petType) {
        return vaccineRepository.findByTargetSpeciesAndIsCore(petType.name(), true);
    }

    /**
     * Get all core vaccines regardless of species
     */
    public List<Vaccine> getAllCoreVaccines() {
        return vaccineRepository.findByIsCore(true);
    }

    /**
     * Get all optional (non-core) vaccines
     */
    public List<Vaccine> getAllOptionalVaccines() {
        return vaccineRepository.findByIsCore(false);
    }

    /**
     * Create a new vaccine
     */
    public Vaccine createVaccine(Vaccine vaccine) {
        return vaccineRepository.save(vaccine);
    }

    /**
     * Update an existing vaccine
     */
    public Vaccine updateVaccine(Long id, Vaccine vaccineDetails) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + id));

        vaccine.setName(vaccineDetails.getName());
        vaccine.setTargetSpecies(vaccineDetails.getTargetSpecies());
        vaccine.setDescription(vaccineDetails.getDescription());
        vaccine.setCore(vaccineDetails.isCore());

        return vaccineRepository.save(vaccine);
    }

    /**
     * Delete a vaccine by ID
     */
    public void deleteVaccine(Long id) {
        vaccineRepository.deleteById(id);
    }

    /**
     * Count total vaccines
     */
    public long countVaccines() {
        return vaccineRepository.count();
    }

    /**
     * Get vaccine statistics by pet type
     */
    public VaccineStatistics getStatisticsForPetType(PetType petType) {
        List<Vaccine> allVaccines = getVaccinesForPetType(petType);
        long coreCount = allVaccines.stream().filter(Vaccine::isCore).count();
        long optionalCount = allVaccines.size() - coreCount;

        return new VaccineStatistics(petType.name(), allVaccines.size(), coreCount, optionalCount);
    }

    /**
     * Inner class for vaccine statistics
     */
    public static class VaccineStatistics {
        private final String petType;
        private final long totalVaccines;
        private final long coreVaccines;
        private final long optionalVaccines;

        public VaccineStatistics(String petType, long totalVaccines, long coreVaccines, long optionalVaccines) {
            this.petType = petType;
            this.totalVaccines = totalVaccines;
            this.coreVaccines = coreVaccines;
            this.optionalVaccines = optionalVaccines;
        }

        // Getters
        public String getPetType() { return petType; }
        public long getTotalVaccines() { return totalVaccines; }
        public long getCoreVaccines() { return coreVaccines; }
        public long getOptionalVaccines() { return optionalVaccines; }

        @Override
        public String toString() {
            return String.format("Tipo: %s | Total: %d | Esenciales: %d | Opcionales: %d",
                    petType, totalVaccines, coreVaccines, optionalVaccines);
        }
    }
}
