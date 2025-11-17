package com.bydaffi.anypetbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue("HAMSTER")
public class Hamster extends Pet {
    // You can add hamster-specific fields here in the future
}
