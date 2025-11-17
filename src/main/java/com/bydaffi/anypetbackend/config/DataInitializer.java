package com.bydaffi.anypetbackend.config;

import com.bydaffi.anypetbackend.models.PetType;
import com.bydaffi.anypetbackend.models.Vaccine;
import com.bydaffi.anypetbackend.repository.VaccineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the database with common vaccines for different pet species.
 * Runs automatically when the application starts.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(VaccineRepository vaccineRepository) {
        return args -> {
            // Only initialize if database is empty
            if (vaccineRepository.count() > 0) {
                System.out.println("Database already contains vaccines. Skipping initialization.");
                return;
            }

            System.out.println("Initializing vaccine database...");

            // ==================== DOG VACCINES ====================
            createVaccine(vaccineRepository,
                "Rabia (Perros)",
                PetType.DOG.name(),
                "Vacuna contra el virus de la rabia. Obligatoria en la mayoría de países. Protege contra una enfermedad mortal transmisible a humanos.",
                true);

            createVaccine(vaccineRepository,
                "Moquillo Canino (Distemper)",
                PetType.DOG.name(),
                "Protege contra el virus del moquillo, una enfermedad viral grave que afecta los sistemas respiratorio, gastrointestinal y nervioso.",
                true);

            createVaccine(vaccineRepository,
                "Parvovirus Canino",
                PetType.DOG.name(),
                "Vacuna contra el parvovirus, altamente contagioso y potencialmente mortal, especialmente en cachorros.",
                true);

            createVaccine(vaccineRepository,
                "Hepatitis Infecciosa Canina (Adenovirus tipo 2)",
                PetType.DOG.name(),
                "Protege contra la hepatitis infecciosa canina, que afecta el hígado, riñones y otros órganos.",
                true);

            createVaccine(vaccineRepository,
                "Leptospirosis",
                PetType.DOG.name(),
                "Protección contra la bacteria leptospira que puede causar insuficiencia renal y hepática. Transmisible a humanos (zoonosis).",
                false);

            createVaccine(vaccineRepository,
                "Bordetella (Tos de las Perreras)",
                PetType.DOG.name(),
                "Vacuna contra la tos de las perreras, infección respiratoria común en perros que conviven con otros.",
                false);

            createVaccine(vaccineRepository,
                "Parainfluenza Canina",
                PetType.DOG.name(),
                "Protege contra uno de los virus que causan la tos de las perreras.",
                false);

            createVaccine(vaccineRepository,
                "Coronavirus Canino",
                PetType.DOG.name(),
                "Protección contra el coronavirus canino, que causa infecciones gastrointestinales.",
                false);

            // ==================== CAT VACCINES ====================
            createVaccine(vaccineRepository,
                "Rabia (Gatos)",
                PetType.CAT.name(),
                "Vacuna contra el virus de la rabia en gatos. Obligatoria en muchas regiones.",
                true);

            createVaccine(vaccineRepository,
                "FVRCP (Trivalente Felina)",
                PetType.CAT.name(),
                "Vacuna triple felina que protege contra Rinotraqueítis (Herpesvirus), Calicivirus y Panleucopenia (moquillo felino).",
                true);

            createVaccine(vaccineRepository,
                "Leucemia Felina (FeLV)",
                PetType.CAT.name(),
                "Protección contra el virus de la leucemia felina, que debilita el sistema inmunológico. Recomendada para gatos que salen al exterior.",
                false);

            createVaccine(vaccineRepository,
                "Clamidia Felina",
                PetType.CAT.name(),
                "Vacuna contra la bacteria Chlamydophila felis, que causa conjuntivitis y problemas respiratorios.",
                false);

            createVaccine(vaccineRepository,
                "Inmunodeficiencia Felina (FIV)",
                PetType.CAT.name(),
                "Vacuna contra el virus de la inmunodeficiencia felina (VIF o FIV), similar al VIH en humanos.",
                false);

            // ==================== RABBIT VACCINES ====================
            createVaccine(vaccineRepository,
                "Mixomatosis",
                PetType.RABBIT.name(),
                "Vacuna contra la mixomatosis, enfermedad viral transmitida por mosquitos y pulgas. Altamente mortal en conejos.",
                true);

            createVaccine(vaccineRepository,
                "Enfermedad Hemorrágica Vírica del Conejo (RHD/VHD)",
                PetType.RABBIT.name(),
                "Protege contra la enfermedad hemorrágica vírica, altamente contagiosa y mortal. Existen variantes RHD1 y RHD2.",
                true);

            createVaccine(vaccineRepository,
                "RHD2 (Variante 2 de Enfermedad Hemorrágica)",
                PetType.RABBIT.name(),
                "Vacuna específica contra la variante RHD2 de la enfermedad hemorrágica vírica.",
                true);

            // ==================== HAMSTER VACCINES ====================
            // Los hamsters generalmente no requieren vacunas rutinarias, pero incluimos opcionales
            createVaccine(vaccineRepository,
                "Rabia (Roedores Exóticos)",
                PetType.HAMSTER.name(),
                "Vacuna contra rabia para roedores exóticos. No es rutinaria pero puede ser requerida en ciertos países o situaciones.",
                false);

            // ==================== TURTLE VACCINES ====================
            // Las tortugas no tienen vacunas rutinarias estandarizadas
            createVaccine(vaccineRepository,
                "Vacuna Experimental para Reptiles",
                PetType.TURTLE.name(),
                "Algunas vacunas experimentales existen para prevenir infecciones bacterianas en reptiles. Consultar con veterinario especializado.",
                false);

            // ==================== PARAKEET VACCINES ====================
            createVaccine(vaccineRepository,
                "Poliomavirus Aviar",
                PetType.PARAKEET.name(),
                "Vacuna contra el poliomavirus aviar, que puede causar enfermedad grave en aves jóvenes.",
                false);

            createVaccine(vaccineRepository,
                "Enfermedad de Newcastle",
                PetType.PARAKEET.name(),
                "Protege contra el virus de Newcastle, enfermedad respiratoria viral altamente contagiosa en aves.",
                false);

            createVaccine(vaccineRepository,
                "Viruela Aviar (Poxvirus)",
                PetType.PARAKEET.name(),
                "Vacuna contra la viruela aviar, transmitida por mosquitos. Causa lesiones en piel y mucosas.",
                false);

            // ==================== DUCK VACCINES ====================
            createVaccine(vaccineRepository,
                "Cólera Aviar (Pasteurella)",
                PetType.DUCK.name(),
                "Vacuna contra la bacteria Pasteurella multocida que causa el cólera aviar en patos.",
                true);

            createVaccine(vaccineRepository,
                "Enfermedad de Newcastle (Patos)",
                PetType.DUCK.name(),
                "Protección contra el virus de Newcastle en patos domésticos.",
                true);

            createVaccine(vaccineRepository,
                "Hepatitis Viral del Pato (DVH)",
                PetType.DUCK.name(),
                "Vacuna contra la hepatitis viral del pato, enfermedad mortal que afecta principalmente a patitos jóvenes.",
                true);

            createVaccine(vaccineRepository,
                "Influenza Aviar",
                PetType.DUCK.name(),
                "Vacuna contra cepas de influenza aviar. Importante en patos domésticos por razones de salud pública.",
                false);

            createVaccine(vaccineRepository,
                "Enteritis Viral del Pato",
                PetType.DUCK.name(),
                "Protege contra la enteritis viral (plague del pato), enfermedad altamente contagiosa.",
                false);

            System.out.println("Vaccine database initialized successfully with " + vaccineRepository.count() + " vaccines.");
        };
    }

    /**
     * Helper method to create and save a vaccine
     */
    private void createVaccine(VaccineRepository repository, String name, String targetSpecies,
                              String description, boolean isCore) {
        Vaccine vaccine = new Vaccine();
        vaccine.setName(name);
        vaccine.setTargetSpecies(targetSpecies);
        vaccine.setDescription(description);
        vaccine.setCore(isCore);
        repository.save(vaccine);
    }
}
