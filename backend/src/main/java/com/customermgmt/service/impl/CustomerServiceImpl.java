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
import com.customermgmt.repository.projection.CustomerListProjection;
import com.customermgmt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        return getCustomerById(saved.getId());
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

        customer.getMobileNumbers().clear();
        customer.getAddresses().clear();
        mapRequestToCustomer(request, customer);

        customerRepository.saveAndFlush(customer);

        return getCustomerById(id);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        // Step 1: load scalars + mobiles + addresses (with city/country) in one query
        Customer customer = customerRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        // Step 2: load familyMembers in a separate query.
        // Hibernate's L1 cache means both queries operate on the same Customer
        // instance — the familyMembers Set is populated on the already-loaded object.
        // This avoids the "cannot simultaneously fetch multiple bags" HibernateException
        // that occurs when you JOIN FETCH two List collections in one query.
        customerRepository.findByIdWithFamilyMembers(id);

        return customerMapper.toResponse(customer);
    }

    @Override
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        // List view: scalar fields only — no collections needed.
        // toListResponse maps only id/name/nicNumber/dateOfBirth/active,
        // so no lazy collections are touched and no LazyInitializationException can occur.
        Page<CustomerListProjection> page = customerRepository.findAllActive(pageable);
        Page<CustomerResponse> responsePage = page.map(this::toListResponse);
        return PagedResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public List<CustomerResponse> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // Search returns shallow results — only scalar fields, safe from lazy loading
        List<Customer> customers = customerRepository.searchByNameOrNic(query.trim());
        return customerMapper.toListResponseList(customers);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private CustomerResponse toListResponse(CustomerListProjection customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .name(customer.getName())
            .dateOfBirth(customer.getDateOfBirth())
            .nicNumber(customer.getNicNumber())
            .active(customer.isActive())
            .mobileCount(safeCount(customer.getMobileCount()))
            .addressCount(safeCount(customer.getAddressCount()))
            .familyMemberCount(safeCount(customer.getFamilyMemberCount()))
            .build();
    }

    private int safeCount(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private void mapRequestToCustomer(CustomerRequest request, Customer customer) {
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());

        if (request.getMobileNumbers() != null) {
            request.getMobileNumbers().forEach(m -> {
                CustomerMobile mobile = new CustomerMobile();
                mobile.setMobileNumber(m.getMobileNumber());
                mobile.setPrimary(m.isPrimary());
                customer.addMobile(mobile);
            });
        }

        if (request.getAddresses() != null) {
            request.getAddresses().forEach(a -> {
                CustomerAddress address = new CustomerAddress();
                address.setAddressLine1(a.getAddressLine1());
                address.setAddressLine2(a.getAddressLine2());
                address.setPrimary(a.isPrimary());
                City city = cityRepository.findByNameIgnoreCase(a.getCityName().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found: " + a.getCityName()));
                address.setCity(city);
                customer.addAddress(address);
            });
        }

        customer.getFamilyMembers().clear();
        if (request.getFamilyMemberIds() != null) {
            List<Long> requestedIds = new ArrayList<>(new HashSet<>(request.getFamilyMemberIds()));
            List<Customer> members = customerRepository.findAllById(requestedIds);
            Set<Long> foundIds = new HashSet<>();

            members.forEach(member -> foundIds.add(member.getId()));

            requestedIds.forEach(memberId -> {
                if (!foundIds.contains(memberId)) {
                    throw new ResourceNotFoundException("Customer", memberId);
                }
            });

            members.forEach(customer::addFamilyMember);
        }
    }
}
