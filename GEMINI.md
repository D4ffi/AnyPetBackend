# GEMINI Project Context: AnyPetBackend

## Project Overview

This project is a Java-based backend application for a service called "AnyPet". It is built using the Spring Boot framework, which suggests it likely exposes a set of REST APIs for a client application.

Key technologies used:
*   **Java 17:** The programming language used.
*   **Spring Boot:** The core framework for building the application.
*   **Spring Data JPA:** For database interaction.
*   **PostgreSQL:** The configured database.
*   **Maven:** The build and dependency management tool.
*   **Lombok:** A library to reduce boilerplate code.

The application's main entry point is the `AnyPetBackendApplication` class.

## Building and Running

This is a standard Maven project. Here are the common commands:

*   **Build:**
    ```bash
    ./mvnw clean install
    ```
*   **Run:**
    ```bash
    ./mvnw spring-boot:run
    ```
*   **Test:**
    ```bash
    ./mvnw test
    ```

## Development Conventions

The project follows standard Spring Boot conventions.

*   **Configuration:** Application properties are likely managed in `src/main/resources/application.properties`.
*   **Source Code:** Java source code is located in `src/main/java`.
*   **Static Content:** Static assets (like images, CSS, or JavaScript) would be placed in `src/main/resources/static`.
*   **Templates:** Server-side templates (if any) would be in `src/main/resources/templates`.
