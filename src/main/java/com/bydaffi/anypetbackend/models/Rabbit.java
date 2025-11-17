package com.bydaffi.anypetbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue("RABBIT")
public class Rabbit extends Pet {
    // You can add rabbit-specific fields here in the future
}
