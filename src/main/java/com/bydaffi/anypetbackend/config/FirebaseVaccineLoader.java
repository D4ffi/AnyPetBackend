package com.bydaffi.anypetbackend.config;

import com.bydaffi.anypetbackend.models.PetType;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Script para cargar las 26 vacunas a Firebase Firestore.
 * DESHABILITADO: Usar en su lugar el script standalone LoadVaccinesToFirestore.java
 * Para habilitar este script, descomenta @Component abajo.
 */
// @Component
public class FirebaseVaccineLoader implements CommandLineRunner {

    private final Firestore firestore;
    private int vaccineCount = 0;

    public FirebaseVaccineLoader(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("Iniciando carga de vacunas a Firebase Firestore...");
        System.out.println("=".repeat(60));

        // Cargar todas las vacunas
        loadDogVaccines();
        loadCatVaccines();
        loadRabbitVaccines();
        loadHamsterVaccines();
        loadTurtleVaccines();
        loadParakeetVaccines();
        loadDuckVaccines();

        System.out.println("=".repeat(60));
        System.out.println("✓ COMPLETADO: " + vaccineCount + " vacunas cargadas exitosamente a Firestore");
        System.out.println("=".repeat(60));
    }

    private void loadDogVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA PERROS (DOG) ---");

        createVaccine(
            "Rabia (Perros)",
            PetType.DOG.name(),
            "Vacuna contra el virus de la rabia. Obligatoria en la mayoría de países. Protege contra una enfermedad mortal transmisible a humanos.",
            true
        );

        createVaccine(
            "Moquillo Canino (Distemper)",
            PetType.DOG.name(),
            "Protege contra el virus del moquillo, una enfermedad viral grave que afecta los sistemas respiratorio, gastrointestinal y nervioso.",
            true
        );

        createVaccine(
            "Parvovirus Canino",
            PetType.DOG.name(),
            "Vacuna contra el parvovirus, altamente contagioso y potencialmente mortal, especialmente en cachorros.",
            true
        );

        createVaccine(
            "Hepatitis Infecciosa Canina (Adenovirus tipo 2)",
            PetType.DOG.name(),
            "Protege contra la hepatitis infecciosa canina, que afecta el hígado, riñones y otros órganos.",
            true
        );

        createVaccine(
            "Leptospirosis",
            PetType.DOG.name(),
            "Protección contra la bacteria leptospira que puede causar insuficiencia renal y hepática. Transmisible a humanos (zoonosis).",
            false
        );

        createVaccine(
            "Bordetella (Tos de las Perreras)",
            PetType.DOG.name(),
            "Vacuna contra la tos de las perreras, infección respiratoria común en perros que conviven con otros.",
            false
        );

        createVaccine(
            "Parainfluenza Canina",
            PetType.DOG.name(),
            "Protege contra uno de los virus que causan la tos de las perreras.",
            false
        );

        createVaccine(
            "Coronavirus Canino",
            PetType.DOG.name(),
            "Protección contra el coronavirus canino, que causa infecciones gastrointestinales.",
            false
        );
    }

    private void loadCatVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA GATOS (CAT) ---");

        createVaccine(
            "Rabia (Gatos)",
            PetType.CAT.name(),
            "Vacuna contra el virus de la rabia en gatos. Obligatoria en muchas regiones.",
            true
        );

        createVaccine(
            "FVRCP (Trivalente Felina)",
            PetType.CAT.name(),
            "Vacuna triple felina que protege contra Rinotraqueítis (Herpesvirus), Calicivirus y Panleucopenia (moquillo felino).",
            true
        );

        createVaccine(
            "Leucemia Felina (FeLV)",
            PetType.CAT.name(),
            "Protección contra el virus de la leucemia felina, que debilita el sistema inmunológico. Recomendada para gatos que salen al exterior.",
            false
        );

        createVaccine(
            "Clamidia Felina",
            PetType.CAT.name(),
            "Vacuna contra la bacteria Chlamydophila felis, que causa conjuntivitis y problemas respiratorios.",
            false
        );

        createVaccine(
            "Inmunodeficiencia Felina (FIV)",
            PetType.CAT.name(),
            "Vacuna contra el virus de la inmunodeficiencia felina (VIF o FIV), similar al VIH en humanos.",
            false
        );
    }

    private void loadRabbitVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA CONEJOS (RABBIT) ---");

        createVaccine(
            "Mixomatosis",
            PetType.RABBIT.name(),
            "Vacuna contra la mixomatosis, enfermedad viral transmitida por mosquitos y pulgas. Altamente mortal en conejos.",
            true
        );

        createVaccine(
            "Enfermedad Hemorrágica Vírica del Conejo (RHD/VHD)",
            PetType.RABBIT.name(),
            "Protege contra la enfermedad hemorrágica vírica, altamente contagiosa y mortal. Existen variantes RHD1 y RHD2.",
            true
        );

        createVaccine(
            "RHD2 (Variante 2 de Enfermedad Hemorrágica)",
            PetType.RABBIT.name(),
            "Vacuna específica contra la variante RHD2 de la enfermedad hemorrágica vírica.",
            true
        );
    }

    private void loadHamsterVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA HÁMSTERS (HAMSTER) ---");

        createVaccine(
            "Rabia (Roedores Exóticos)",
            PetType.HAMSTER.name(),
            "Vacuna contra rabia para roedores exóticos. No es rutinaria pero puede ser requerida en ciertos países o situaciones.",
            false
        );
    }

    private void loadTurtleVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA TORTUGAS (TURTLE) ---");

        createVaccine(
            "Vacuna Experimental para Reptiles",
            PetType.TURTLE.name(),
            "Algunas vacunas experimentales existen para prevenir infecciones bacterianas en reptiles. Consultar con veterinario especializado.",
            false
        );
    }

    private void loadParakeetVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA PERIQUITOS (PARAKEET) ---");

        createVaccine(
            "Poliomavirus Aviar",
            PetType.PARAKEET.name(),
            "Vacuna contra el poliomavirus aviar, que puede causar enfermedad grave en aves jóvenes.",
            false
        );

        createVaccine(
            "Enfermedad de Newcastle",
            PetType.PARAKEET.name(),
            "Protege contra el virus de Newcastle, enfermedad respiratoria viral altamente contagiosa en aves.",
            false
        );

        createVaccine(
            "Viruela Aviar (Poxvirus)",
            PetType.PARAKEET.name(),
            "Vacuna contra la viruela aviar, transmitida por mosquitos. Causa lesiones en piel y mucosas.",
            false
        );
    }

    private void loadDuckVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n--- VACUNAS PARA PATOS (DUCK) ---");

        createVaccine(
            "Cólera Aviar (Pasteurella)",
            PetType.DUCK.name(),
            "Vacuna contra la bacteria Pasteurella multocida que causa el cólera aviar en patos.",
            true
        );

        createVaccine(
            "Enfermedad de Newcastle (Patos)",
            PetType.DUCK.name(),
            "Protección contra el virus de Newcastle en patos domésticos.",
            true
        );

        createVaccine(
            "Hepatitis Viral del Pato (DVH)",
            PetType.DUCK.name(),
            "Vacuna contra la hepatitis viral del pato, enfermedad mortal que afecta principalmente a patitos jóvenes.",
            true
        );

        createVaccine(
            "Influenza Aviar",
            PetType.DUCK.name(),
            "Vacuna contra cepas de influenza aviar. Importante en patos domésticos por razones de salud pública.",
            false
        );

        createVaccine(
            "Enteritis Viral del Pato",
            PetType.DUCK.name(),
            "Protege contra la enteritis viral (plague del pato), enfermedad altamente contagiosa.",
            false
        );
    }

    /**
     * Crea y guarda una vacuna en Firestore
     */
    private void createVaccine(String name, String targetSpecies, String description, boolean isCore)
            throws ExecutionException, InterruptedException {

        Map<String, Object> vaccineData = new HashMap<>();
        vaccineData.put("name", name);
        vaccineData.put("targetSpecies", targetSpecies);
        vaccineData.put("description", description);
        vaccineData.put("isCore", isCore);

        // Usar el nombre como ID del documento (sanitizado)
        String docId = sanitizeDocumentId(name);

        WriteResult result = firestore.collection("vaccines")
                .document(docId)
                .set(vaccineData)
                .get();

        vaccineCount++;
        System.out.println(vaccineCount + ". ✓ " + name + " (ID: " + docId + ")");
    }

    /**
     * Sanitiza el nombre de la vacuna para usarlo como ID de documento en Firestore
     */
    private String sanitizeDocumentId(String name) {
        return name.toLowerCase()
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
                .replace("/", "_")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }
}
