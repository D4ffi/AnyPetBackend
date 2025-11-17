package com.bydaffi.anypetbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue("TURTLE")
public class Turtle extends Pet {
    // You can add turtle-specific fields here in the future
}
