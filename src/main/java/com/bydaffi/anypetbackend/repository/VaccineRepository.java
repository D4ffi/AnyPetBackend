package com.bydaffi.anypetbackend.repository;

import com.bydaffi.anypetbackend.models.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    /**
     * Find all vaccines for a specific species
     * @param targetSpecies the species to filter by (e.g., "DOG", "CAT")
     * @return list of vaccines for that species
     */
    List<Vaccine> findByTargetSpecies(String targetSpecies);

    /**
     * Find all core (essential) vaccines
     * @param isCore true for core vaccines, false for optional
     * @return list of core or optional vaccines
     */
    List<Vaccine> findByIsCore(boolean isCore);

    /**
     * Find vaccines by species and core status
     * @param targetSpecies the species
     * @param isCore core status
     * @return filtered list of vaccines
     */
    List<Vaccine> findByTargetSpeciesAndIsCore(String targetSpecies, boolean isCore);

    /**
     * Check if a vaccine with this name already exists
     * @param name vaccine name
     * @return true if exists
     */
    boolean existsByName(String name);
}
