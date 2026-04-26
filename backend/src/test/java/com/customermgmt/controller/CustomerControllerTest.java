package com.customermgmt.controller;

import com.customermgmt.dto.request.CustomerRequest;
import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.dto.response.PagedResponse;
import com.customermgmt.exception.DuplicateResourceException;
import com.customermgmt.exception.GlobalExceptionHandler;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)   // bring in @RestControllerAdvice for 404/409 handling
@DisplayName("CustomerController MockMvc tests")
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CustomerService customerService;

    // Build ObjectMapper manually so it handles LocalDate correctly.
    // @WebMvcTest does NOT auto-configure the full Spring context, so we
    // can't rely on auto-configuration of JavaTimeModule here.
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CustomerRequest validRequest;
    private CustomerResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = CustomerRequest.builder()
            .name("Sunil Fernando")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .nicNumber("851234567V")
            .mobileNumbers(Collections.emptyList())
            .addresses(Collections.emptyList())
            .build();

        sampleResponse = CustomerResponse.builder()
            .id(1L)
            .name("Sunil Fernando")
            .nicNumber("851234567V")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .active(true)
            .mobileNumbers(Collections.emptyList())
            .addresses(Collections.emptyList())
            .familyMembers(Collections.emptyList())
            .build();
    }

    // ------------------------------------------------------------------
    // POST /api/v1/customers
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST — 201 Created on valid request")
    void createCustomer_valid_returns201() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Sunil Fernando"))
            .andExpect(jsonPath("$.data.nicNumber").value("851234567V"));
    }

    @Test
    @DisplayName("POST — 400 Bad Request when name is blank")
    void createCustomer_blankName_returns400() throws Exception {
        validRequest.setName("");

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST — 400 Bad Request when NIC is blank")
    void createCustomer_blankNic_returns400() throws Exception {
        validRequest.setNicNumber("");

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — 409 Conflict on duplicate NIC")
    void createCustomer_duplicateNic_returns409() throws Exception {
        when(customerService.createCustomer(any()))
            .thenThrow(new DuplicateResourceException("NIC 851234567V already exists"));

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("NIC 851234567V already exists"));
    }

    // ------------------------------------------------------------------
    // GET /api/v1/customers/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /{id} — 200 with full customer response")
    void getCustomer_found_returns200() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/customers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("Sunil Fernando"));
    }

    @Test
    @DisplayName("GET /{id} — 404 when customer does not exist")
    void getCustomer_notFound_returns404() throws Exception {
        when(customerService.getCustomerById(99L))
            .thenThrow(new ResourceNotFoundException("Customer", 99L));

        mockMvc.perform(get("/api/v1/customers/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // GET /api/v1/customers  (paginated list)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET / — 200 with paginated list")
    void getAllCustomers_returns200WithPage() throws Exception {
        PagedResponse<CustomerResponse> paged = PagedResponse.<CustomerResponse>builder()
            .content(Collections.singletonList(sampleResponse))
            .page(0).size(20).totalElements(1).totalPages(1).last(true)
            .build();

        when(customerService.getAllCustomers(any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/customers").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].name").value("Sunil Fernando"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    // ------------------------------------------------------------------
    // PUT /api/v1/customers/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PUT /{id} — 200 on valid update")
    void updateCustomer_valid_returns200() throws Exception {
        when(customerService.updateCustomer(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /{id} — 404 when customer does not exist")
    void updateCustomer_notFound_returns404() throws Exception {
        when(customerService.updateCustomer(eq(99L), any()))
            .thenThrow(new ResourceNotFoundException("Customer", 99L));

        mockMvc.perform(put("/api/v1/customers/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // DELETE /api/v1/customers/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /{id} — 200 soft delete")
    void deleteCustomer_returns200() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/v1/customers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /{id} — 404 when customer not found")
    void deleteCustomer_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Customer", 99L))
            .when(customerService).deleteCustomer(99L);

        mockMvc.perform(delete("/api/v1/customers/99"))
            .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // GET /api/v1/customers/search
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /search — 200 with matching customers")
    void searchCustomers_returns200() throws Exception {
        when(customerService.searchCustomers("Sunil"))
            .thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/customers/search").param("q", "Sunil"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Sunil Fernando"));
    }
}
