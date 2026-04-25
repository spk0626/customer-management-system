package com.customermgmt.repository;

import com.customermgmt.entity.Customer;
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
     * Fetch customer with all collections in one query.
     * Using LEFT JOIN FETCH to avoid N+1 — one DB call for all associated data.
     */
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.mobileNumbers " +
           "LEFT JOIN FETCH c.addresses a " +
           "LEFT JOIN FETCH a.city ct " +
           "LEFT JOIN FETCH ct.country " +
           "WHERE c.id = :id AND c.active = true")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    /**
     * Paginated list — fetches only scalar fields, no collections, for table view performance.
     */
    @Query("SELECT c FROM Customer c WHERE c.active = true")
    Page<Customer> findAllActive(Pageable pageable);

    /**
     * Search by name or NIC — used for family member lookup.
     */
    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.nicNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Customer> searchByNameOrNic(@Param("query") String query);

    /**
     * Bulk NIC lookup — used during bulk import to detect duplicates in one query.
     */
    @Query("SELECT c.nicNumber FROM Customer c WHERE c.nicNumber IN :nics")
    List<String> findExistingNics(@Param("nics") List<String> nics);

    Optional<Customer> findByNicNumber(String nicNumber);
}
