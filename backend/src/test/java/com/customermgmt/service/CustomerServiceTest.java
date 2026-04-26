package com.customermgmt.service;

import com.customermgmt.dto.request.CustomerRequest;
import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.entity.Customer;
import com.customermgmt.exception.DuplicateResourceException;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.mapper.CustomerMapper;
import com.customermgmt.repository.CityRepository;
import com.customermgmt.repository.CustomerRepository;
import com.customermgmt.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService unit tests")
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CityRepository cityRepository;
    @Mock private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRequest validRequest;
    private Customer savedCustomer;
    private CustomerResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = CustomerRequest.builder()
            .name("Kamal Perera")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .nicNumber("901234567V")
            .mobileNumbers(Collections.emptyList())
            .addresses(Collections.emptyList())
            .familyMemberIds(Collections.emptyList())
            .build();

        savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("Kamal Perera");
        savedCustomer.setDateOfBirth(LocalDate.of(1990, 5, 15));
        savedCustomer.setNicNumber("901234567V");

        expectedResponse = CustomerResponse.builder()
            .id(1L)
            .name("Kamal Perera")
            .nicNumber("901234567V")
            .build();
    }

    // ---- createCustomer ----

    @Test
    @DisplayName("createCustomer — success: saves and returns response")
    void createCustomer_success() {
        when(customerRepository.existsByNicNumber("901234567V")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerMapper.toResponse(savedCustomer)).thenReturn(expectedResponse);

        CustomerResponse result = customerService.createCustomer(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kamal Perera");
        assertThat(result.getNicNumber()).isEqualTo("901234567V");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("createCustomer — fails when NIC already exists")
    void createCustomer_duplicateNic_throws() {
        when(customerRepository.existsByNicNumber("901234567V")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(validRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("901234567V");

        verify(customerRepository, never()).save(any());
    }

    // ---- getCustomerById ----

    @Test
    @DisplayName("getCustomerById — returns mapped response when customer exists")
    void getCustomerById_found() {
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerMapper.toResponse(savedCustomer)).thenReturn(expectedResponse);

        CustomerResponse result = customerService.getCustomerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("getCustomerById — throws ResourceNotFoundException when not found")
    void getCustomerById_notFound() {
        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---- updateCustomer ----

    @Test
    @DisplayName("updateCustomer — success: updates fields and returns new response")
    void updateCustomer_success() {
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByNicNumberAndIdNot("901234567V", 1L)).thenReturn(false);
        when(customerRepository.saveAndFlush(any())).thenReturn(savedCustomer);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerMapper.toResponse(savedCustomer)).thenReturn(expectedResponse);

        CustomerResponse result = customerService.updateCustomer(1L, validRequest);

        assertThat(result).isNotNull();
        verify(customerRepository).saveAndFlush(any(Customer.class));
    }

    @Test
    @DisplayName("updateCustomer — throws when NIC conflicts with another customer")
    void updateCustomer_nicConflict_throws() {
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByNicNumberAndIdNot("901234567V", 1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, validRequest))
            .isInstanceOf(DuplicateResourceException.class);
    }

    // ---- deleteCustomer ----

    @Test
    @DisplayName("deleteCustomer — soft deletes by setting active=false")
    void deleteCustomer_softDelete() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));

        customerService.deleteCustomer(1L);

        assertThat(savedCustomer.isActive()).isFalse();
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    @DisplayName("deleteCustomer — throws when customer not found")
    void deleteCustomer_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- searchCustomers ----

    @Test
    @DisplayName("searchCustomers — returns empty list for blank query")
    void searchCustomers_blankQuery_returnsEmpty() {
        assertThat(customerService.searchCustomers("  ")).isEmpty();
        assertThat(customerService.searchCustomers(null)).isEmpty();
        verify(customerRepository, never()).searchByNameOrNic(any());
    }
}
