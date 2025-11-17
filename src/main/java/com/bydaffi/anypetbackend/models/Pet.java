package com.bydaffi.anypetbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "pet_type")
public abstract class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String race;
    private int age;
    private float weight;
    private boolean healthStatus;

    /**
     * URL of the pet's profile image stored in S3
     */
    @Column(length = 500)
    private String profileImageUrl;

    /**
     * URL of the pet's profile thumbnail stored in S3
     */
    @Column(length = 500)
    private String profileThumbnailUrl;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaccinationRecord> vaccinationRecords = new ArrayList<>();

}
