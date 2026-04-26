package com.customermgmt.repository;

import com.customermgmt.entity.Customer;
import com.customermgmt.entity.CustomerMobile;
import com.customermgmt.repository.projection.CustomerListProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerRepository slice tests")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        customer1 = new Customer();
        customer1.setName("Nimal Silva");
        customer1.setDateOfBirth(LocalDate.of(1992, 7, 10));
        customer1.setNicNumber("921234567V");
        customer1.setActive(true);

        customer2 = new Customer();
        customer2.setName("Priya Rajapaksa");
        customer2.setDateOfBirth(LocalDate.of(1988, 1, 25));
        customer2.setNicNumber("881234567V");
        customer2.setActive(true);

        customerRepository.saveAll(Arrays.asList(customer1, customer2));
    }

    @Test
    @DisplayName("existsByNicNumber — returns true for existing NIC")
    void existsByNicNumber_existing_returnsTrue() {
        assertThat(customerRepository.existsByNicNumber("921234567V")).isTrue();
    }

    @Test
    @DisplayName("existsByNicNumber — returns false for new NIC")
    void existsByNicNumber_new_returnsFalse() {
        assertThat(customerRepository.existsByNicNumber("999999999V")).isFalse();
    }

    @Test
    @DisplayName("existsByNicNumberAndIdNot — ignores same customer when checking")
    void existsByNicNumberAndIdNot_sameCustomer_returnsFalse() {
        assertThat(customerRepository.existsByNicNumberAndIdNot(
            "921234567V", customer1.getId())).isFalse();
    }

    @Test
    @DisplayName("findByNicNumber — returns correct customer")
    void findByNicNumber_found() {
        Optional<Customer> result = customerRepository.findByNicNumber("881234567V");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Priya Rajapaksa");
    }

    @Test
    @DisplayName("searchByNameOrNic — matches partial name case-insensitively")
    void searchByNameOrNic_partialName_found() {
        List<Customer> results = customerRepository.searchByNameOrNic("nimal");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Nimal Silva");
    }

    @Test
    @DisplayName("searchByNameOrNic — matches partial NIC")
    void searchByNameOrNic_partialNic_found() {
        List<Customer> results = customerRepository.searchByNameOrNic("8812");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNicNumber()).isEqualTo("881234567V");
    }

    @Test
    @DisplayName("findExistingNics — returns only NICs that exist in DB")
    void findExistingNics_returnsOnlyExisting() {
        List<String> toCheck = Arrays.asList("921234567V", "881234567V", "000000000X");
        List<String> found = customerRepository.findExistingNics(toCheck);
        assertThat(found).containsExactlyInAnyOrder("921234567V", "881234567V");
        assertThat(found).doesNotContain("000000000X");
    }

    @Test
    @DisplayName("findAllActive - returns related record counts for table view")
    void findAllActive_returnsCounts() {
        CustomerMobile mobile = new CustomerMobile();
        mobile.setMobileNumber("0771234567");
        mobile.setPrimary(true);
        customer1.addMobile(mobile);
        customerRepository.saveAndFlush(customer1);

        Page<CustomerListProjection> page = customerRepository.findAllActive(PageRequest.of(0, 10));
        CustomerListProjection result = page.getContent().stream()
            .filter(customer -> "921234567V".equals(customer.getNicNumber()))
            .findFirst()
            .orElseThrow(AssertionError::new);

        assertThat(result.getMobileCount()).isEqualTo(1L);
        assertThat(result.getAddressCount()).isEqualTo(0L);
        assertThat(result.getFamilyMemberCount()).isEqualTo(0L);
    }
}
