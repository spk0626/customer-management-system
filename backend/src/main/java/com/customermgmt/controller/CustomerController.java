package com.customermgmt.controller;

import com.customermgmt.dto.request.CustomerRequest;
import com.customermgmt.dto.response.ApiResponse;
import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.dto.response.PagedResponse;
import com.customermgmt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * GET /api/v1/customers?page=0&size=20&sort=name,asc
     * Returns paginated list for table view.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        PagedResponse<CustomerResponse> result = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/v1/customers/{id}
     * Returns full customer detail including mobiles, addresses, family members.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerById(id)));
    }

    /**
     * GET /api/v1/customers/search?q=somename
     * Lightweight search for family member selector.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> searchCustomers(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.searchCustomers(q)));
    }

    /**
     * POST /api/v1/customers
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Customer created successfully", created));
    }

    /**
     * PUT /api/v1/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", updated));
    }

    /**
     * DELETE /api/v1/customers/{id}
     * Soft delete — sets is_active = false.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deactivated", null));
    }
}