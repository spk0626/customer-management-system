package com.customermgmt.service;

import com.customermgmt.dto.request.CustomerRequest;
import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable);

    void deleteCustomer(Long id);

    List<CustomerResponse> searchCustomers(String query);
}
