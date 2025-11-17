package com.bydaffi.anypetbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue("PARAKEET")
public class Parakeet extends Pet {
    // You can add parakeet-specific fields here in the future
}
