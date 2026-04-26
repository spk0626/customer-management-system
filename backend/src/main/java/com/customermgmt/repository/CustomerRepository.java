package com.customermgmt.repository;

import com.customermgmt.entity.Customer;
import com.customermgmt.repository.projection.CustomerListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByNicNumber(String nicNumber);

    boolean existsByNicNumberAndIdNot(String nicNumber, Long id);

    /**
     * Fetch ONE customer with ALL collections eagerly loaded in a single query.
     * familyMembers added — was missing, caused LazyInitializationException
     * when the mapper accessed the Set<Customer> outside a transaction.
     *
     * Note: JPA does not allow two collection JOIN FETCHes in one query when
     * both are bags (List). We solve this by fetching familyMembers in a
     * separate query below and letting Hibernate's first-level cache merge them.
     */
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.mobileNumbers " +
           "LEFT JOIN FETCH c.addresses a " +
           "LEFT JOIN FETCH a.city ct " +
           "LEFT JOIN FETCH ct.country " +
           "WHERE c.id = :id AND c.active = true")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    /**
     * Separate query to eagerly load familyMembers for a given customer.
     * Called alongside findByIdWithDetails in the service — Hibernate's
     * L1 cache merges the result into the same Customer instance.
     */
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.familyMembers fm " +
           "WHERE c.id = :id AND c.active = true")
    Optional<Customer> findByIdWithFamilyMembers(@Param("id") Long id);

    /**
     * Paginated list — scalar fields only, no collections.
     * List page does not need mobiles/addresses/familyMembers.
     */
    @Query(
        value = "SELECT c.id AS id, c.name AS name, c.dateOfBirth AS dateOfBirth, " +
            "c.nicNumber AS nicNumber, c.active AS active, " +
            "COUNT(DISTINCT m.id) AS mobileCount, " +
            "COUNT(DISTINCT a.id) AS addressCount, " +
            "COUNT(DISTINCT fm.id) AS familyMemberCount " +
            "FROM Customer c " +
            "LEFT JOIN c.mobileNumbers m " +
            "LEFT JOIN c.addresses a " +
            "LEFT JOIN c.familyMembers fm " +
            "WHERE c.active = true " +
            "GROUP BY c.id, c.name, c.dateOfBirth, c.nicNumber, c.active",
        countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.active = true"
    )
    Page<CustomerListProjection> findAllActive(Pageable pageable);

    /**
     * Search by name or NIC — for family member selector dropdown.
     */
    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.nicNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Customer> searchByNameOrNic(@Param("query") String query);

    /**
     * Bulk NIC lookup — detects duplicates in one query during import.
     */
    @Query("SELECT c.nicNumber FROM Customer c WHERE c.nicNumber IN :nics")
    List<String> findExistingNics(@Param("nics") List<String> nics);

    List<Customer> findByNicNumberIn(List<String> nicNumbers);

    Optional<Customer> findByNicNumber(String nicNumber);
}
