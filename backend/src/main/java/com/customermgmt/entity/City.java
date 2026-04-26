package com.customermgmt.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(
    name = "city",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_city_name_country", columnNames = {"name", "country_id"})
    },
    indexes = {
        @Index(name = "idx_city_country", columnList = "country_id")
    }
)
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
