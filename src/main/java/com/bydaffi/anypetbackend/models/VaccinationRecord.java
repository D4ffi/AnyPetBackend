package com.bydaffi.anypetbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
public class VaccinationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    private LocalDate vaccinationDate;

    private LocalDate nextDueDate;

    private String veterinarian;

    /**
     * URL of the vaccine batch/lot image stored in S3
     * (Photo of the vaccine vial/box showing batch number)
     */
    @Column(length = 500)
    private String batchImageUrl;

    /**
     * URL of the batch image thumbnail stored in S3
     */
    @Column(length = 500)
    private String batchThumbnailUrl;

    /**
     * Batch/Lot number of the vaccine
     */
    private String batchNumber;
}
