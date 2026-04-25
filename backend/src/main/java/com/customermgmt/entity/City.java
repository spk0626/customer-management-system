package com.customermgmt.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "city", indexes = {
    @Index(name = "idx_city_country", columnList = "country_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
}
