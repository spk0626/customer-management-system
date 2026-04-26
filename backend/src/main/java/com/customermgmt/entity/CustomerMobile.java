package com.customermgmt.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "customer_mobile", indexes = {
    @Index(name = "idx_mobile_customer", columnList = "customer_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomerMobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;
}
