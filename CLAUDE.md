# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AnyPetBackend is a Spring Boot 3.5.7 REST API backend for managing pet data and vaccination records. The application uses Java 17, JPA for data persistence, and integrates with Firebase Authentication.

## Build and Development Commands

Maven wrapper (`./mvnw`) is used for all build operations:

```bash
# Build the project
./mvnw clean install

# Run the application (starts on default Spring Boot port 8080)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=ClassName

# Run a specific test method
./mvnw test -Dtest=ClassName#methodName

# Package without running tests
./mvnw clean package -DskipTests
```

## Architecture and Key Design Patterns

### Database Configuration

The application is configured to use **H2 in-memory database** for development (see `application.properties`). PostgreSQL is included as a runtime dependency for production use. The H2 console is enabled at `/h2-console` for development debugging.

### Domain Model - Single Table Inheritance

The pet domain uses JPA's Single Table Inheritance pattern (`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`):

- `Pet` (abstract base class) - Contains common pet attributes and a discriminator column `pet_type`
- `Cat` extends `Pet` - Discriminator value: "CAT"
- Future pet types (Dog, Bird, etc.) should extend `Pet` and specify their discriminator value

All pet subtypes are stored in a single database table with a discriminator column to identify the type.

### Entity Relationships

**Pet ↔ VaccinationRecord** (One-to-Many):
- `Pet.vaccinationRecords` - List of all vaccination records for a pet
- Uses `cascade = CascadeType.ALL, orphanRemoval = true` (deleting a pet deletes its records)
- Bidirectional: `VaccinationRecord.pet` references back with `@ManyToOne(fetch = FetchType.LAZY)`

**Vaccine ↔ VaccinationRecord** (One-to-Many):
- A `Vaccine` entity defines vaccine types (e.g., "Rabies", "FVRCP")
- `VaccinationRecord` links a specific pet to a specific vaccine with administration details
- `VaccinationRecord.vaccine` uses lazy fetching

### Firebase Integration

Firebase Authentication is configured via `FirebaseConfig.java`:
- Credentials are loaded from the `GOOGLE_APPLICATION_CREDENTIALS` environment variable
- The `.secrets/firebase-credentials.json` file contains service account credentials
- Set environment variable: `export GOOGLE_APPLICATION_CREDENTIALS=.secrets/firebase-credentials.json`
- `FirebaseAuth` bean is available for token verification and authentication in controllers

### Package Structure

```
com.bydaffi.anypetbackend/
├── config/          - Spring configuration beans (Firebase setup)
├── controller/      - REST API controllers
├── models/          - JPA entities (Pet, Cat, Vaccine, VaccinationRecord)
└── AnyPetBackendApplication.java - Main application entry point
```

## Development Notes

- **Lombok is enabled**: Use `@Getter`, `@Setter`, `@NoArgsConstructor` annotations. The Maven compiler plugin is configured to process Lombok annotations.
- **JPA/Hibernate**: Entities use standard JPA annotations. Hibernate is the default JPA provider via Spring Boot.
- When adding new pet types, extend `Pet` and add a `@DiscriminatorValue` annotation.
- When adding new controllers, follow the pattern in `HealthController` (simple `@RestController` with request mappings).
