package com.customermgmt.service.impl;

import com.customermgmt.dto.request.CustomerRequest;
import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.dto.response.PagedResponse;
import com.customermgmt.entity.*;
import com.customermgmt.exception.DuplicateResourceException;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.mapper.CustomerMapper;
import com.customermgmt.repository.CityRepository;
import com.customermgmt.repository.CustomerRepository;
import com.customermgmt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // default read-only; write methods override below
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByNicNumber(request.getNicNumber())) {
            throw new DuplicateResourceException(
                "Customer with NIC " + request.getNicNumber() + " already exists");
        }

        Customer customer = new Customer();
        mapRequestToCustomer(request, customer);

        Customer saved = customerRepository.save(customer);
        // Re-fetch with full details to return complete response in one query
        return customerMapper.toResponse(
            customerRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", saved.getId()))
        );
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (customerRepository.existsByNicNumberAndIdNot(request.getNicNumber(), id)) {
            throw new DuplicateResourceException(
                "NIC " + request.getNicNumber() + " is already used by another customer");
        }

        // Clear and rebuild child collections — orphanRemoval handles deletions
        customer.getMobileNumbers().clear();
        customer.getAddresses().clear();

        mapRequestToCustomer(request, customer);

        // Re-fetch with full details after flush
        customerRepository.saveAndFlush(customer);
        return customerMapper.toResponse(
            customerRepository.findByIdWithDetails(id).orElseThrow()
        );
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        // Single query with all JOINs — no N+1
        Customer customer = customerRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return customerMapper.toResponse(customer);
    }

    @Override
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        // Table view only needs scalar fields — no JOIN FETCH needed here
        Page<Customer> page = customerRepository.findAllActive(pageable);
        Page<CustomerResponse> responsePage = page.map(customerMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        // Soft delete — preserves referential integrity for family member relationships
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public List<CustomerResponse> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Customer> customers = customerRepository.searchByNameOrNic(query.trim());
        return customerMapper.toResponseList(customers);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a CustomerRequest onto an existing (or new) Customer entity.
     * City lookups use a single query per address — city is already in memory
     * after first access due to first-level cache within the transaction.
     */
    private void mapRequestToCustomer(CustomerRequest request, Customer customer) {
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());

        // Mobile numbers
        if (request.getMobileNumbers() != null) {
            request.getMobileNumbers().forEach(m -> {
                CustomerMobile mobile = new CustomerMobile();
                mobile.setMobileNumber(m.getMobileNumber());
                mobile.setPrimary(m.isPrimary());
                customer.addMobile(mobile);
            });
        }

        // Addresses — city lookup hits master table once per city (cached by JPA L1)
        if (request.getAddresses() != null) {
            request.getAddresses().forEach(a -> {
                CustomerAddress address = new CustomerAddress();
                address.setAddressLine1(a.getAddressLine1());
                address.setAddressLine2(a.getAddressLine2());
                address.setPrimary(a.isPrimary());
                if (a.getCityId() != null) {
                    City city = cityRepository.findById(a.getCityId())
                        .orElseThrow(() -> new ResourceNotFoundException("City", a.getCityId()));
                    address.setCity(city);
                }
                customer.addAddress(address);
            });
        }

        // Family members — resolved by ID, no extra join needed
        customer.getFamilyMembers().clear();
        if (request.getFamilyMemberIds() != null) {
            request.getFamilyMemberIds().forEach(memberId -> {
                Customer member = customerRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", memberId));
                customer.addFamilyMember(member);
            });
        }
    }
}