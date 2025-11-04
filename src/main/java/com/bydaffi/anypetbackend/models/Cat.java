package com.bydaffi.anypetbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue("CAT")
public class Cat extends Pet {
    // You can add cat-specific fields here in the future
}
