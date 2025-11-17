package com.bydaffi.anypetbackend.models;

/**
 * Enum representing the different types of pets supported by the system.
 * Used for consistency when categorizing vaccines and pets.
 */
public enum PetType {
    DOG("Perro", "Dog"),
    CAT("Gato", "Cat"),
    HAMSTER("Hamster", "Hamster"),
    TURTLE("Tortuga", "Turtle"),
    RABBIT("Conejo", "Rabbit"),
    PARAKEET("Periquito", "Parakeet"),
    DUCK("Pato", "Duck"),
    ANY("Cualquiera", "Any"); // For vaccines that apply to multiple species

    private final String spanishName;
    private final String englishName;

    PetType(String spanishName, String englishName) {
        this.spanishName = spanishName;
        this.englishName = englishName;
    }

    public String getSpanishName() {
        return spanishName;
    }

    public String getEnglishName() {
        return englishName;
    }
}
