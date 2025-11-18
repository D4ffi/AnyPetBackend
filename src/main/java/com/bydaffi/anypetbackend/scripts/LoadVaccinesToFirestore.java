package com.bydaffi.anypetbackend.scripts;

import com.bydaffi.anypetbackend.models.PetType;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Script standalone para cargar las 26 vacunas a Firebase Firestore.
 *
 * REQUISITOS:
 * 1. Tener el archivo de credenciales Firebase en .secrets/firebase-authkey.json
 * 2. Ejecutar con: mvn exec:java -Dexec.mainClass="com.bydaffi.anypetbackend.scripts.LoadVaccinesToFirestore"
 *
 * O compilar y ejecutar directamente:
 * javac -cp "target/classes:target/dependency/*" src/main/java/com/bydaffi/anypetbackend/scripts/LoadVaccinesToFirestore.java
 * java -cp "target/classes:target/dependency/*" com.bydaffi.anypetbackend.scripts.LoadVaccinesToFirestore
 */
public class LoadVaccinesToFirestore {

    private static Firestore firestore;
    private static int vaccineCount = 0;

    public static void main(String[] args) {
        try {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("  SCRIPT DE CARGA DE VACUNAS A FIREBASE FIRESTORE");
            System.out.println("=".repeat(70));

            // Inicializar Firebase
            initializeFirebase();

            // Cargar todas las vacunas
            loadAllVaccines();

            System.out.println("\n" + "=".repeat(70));
            System.out.println("  ✓ COMPLETADO: " + vaccineCount + " vacunas cargadas exitosamente");
            System.out.println("=".repeat(70) + "\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeFirebase() throws IOException {
        System.out.println("\n→ Inicializando conexión con Firebase...");

        String credentialsPath = ".secrets/firebase-authkey.json";
        FileInputStream serviceAccount = new FileInputStream(credentialsPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        firestore = FirestoreClient.getFirestore();
        System.out.println("✓ Conexión establecida con Firestore");
    }

    private static void loadAllVaccines() throws ExecutionException, InterruptedException {
        loadDogVaccines();
        loadCatVaccines();
        loadRabbitVaccines();
        loadHamsterVaccines();
        loadTurtleVaccines();
        loadParakeetVaccines();
        loadDuckVaccines();
    }

    private static void loadDogVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA PERROS (DOG) - 8 vacunas");
        System.out.println("─".repeat(70));

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

    private static void loadCatVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA GATOS (CAT) - 5 vacunas");
        System.out.println("─".repeat(70));

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

    private static void loadRabbitVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA CONEJOS (RABBIT) - 3 vacunas");
        System.out.println("─".repeat(70));

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

    private static void loadHamsterVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA HÁMSTERS (HAMSTER) - 1 vacuna");
        System.out.println("─".repeat(70));

        createVaccine(
            "Rabia (Roedores Exóticos)",
            PetType.HAMSTER.name(),
            "Vacuna contra rabia para roedores exóticos. No es rutinaria pero puede ser requerida en ciertos países o situaciones.",
            false
        );
    }

    private static void loadTurtleVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA TORTUGAS (TURTLE) - 1 vacuna");
        System.out.println("─".repeat(70));

        createVaccine(
            "Vacuna Experimental para Reptiles",
            PetType.TURTLE.name(),
            "Algunas vacunas experimentales existen para prevenir infecciones bacterianas en reptiles. Consultar con veterinario especializado.",
            false
        );
    }

    private static void loadParakeetVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA PERIQUITOS (PARAKEET) - 3 vacunas");
        System.out.println("─".repeat(70));

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

    private static void loadDuckVaccines() throws ExecutionException, InterruptedException {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  VACUNAS PARA PATOS (DUCK) - 5 vacunas");
        System.out.println("─".repeat(70));

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

    private static void createVaccine(String name, String targetSpecies, String description, boolean isCore)
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
        String coreLabel = isCore ? "[CORE]" : "[OPCIONAL]";
        System.out.printf("  %2d. ✓ %-50s %s%n", vaccineCount, name, coreLabel);
    }

    private static String sanitizeDocumentId(String name) {
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
