package com.customermgmt.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_nic", columnList = "nic_number", unique = true),
    @Index(name = "idx_customer_name", columnList = "name")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotNull(message = "Date of birth is mandatory")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "NIC number is mandatory")
    @Column(name = "nic_number", nullable = false, unique = true, length = 20)
    private String nicNumber;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CustomerMobile> mobileNumbers = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CustomerAddress> addresses = new ArrayList<>();

    /**
     * Family members - stored as a join table to avoid circular foreign keys.
     * Both directions are maintained for bidirectional navigation.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "customer_family",
        joinColumns = @JoinColumn(name = "customer_id"),
        inverseJoinColumns = @JoinColumn(name = "family_member_id")
    )
    private Set<Customer> familyMembers = new HashSet<>();

    // Helper methods to keep collections in sync
    public void addMobile(CustomerMobile mobile) {
        mobile.setCustomer(this);
        this.mobileNumbers.add(mobile);
    }

    public void addAddress(CustomerAddress address) {
        address.setCustomer(this);
        this.addresses.add(address);
    }

    public void addFamilyMember(Customer member) {
        this.familyMembers.add(member);
        member.getFamilyMembers().add(this);
    }

    public void removeFamilyMember(Customer member) {
        this.familyMembers.remove(member);
        member.getFamilyMembers().remove(this);
    }
}