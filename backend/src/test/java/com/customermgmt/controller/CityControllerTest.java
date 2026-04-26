package com.customermgmt.controller;

import com.customermgmt.dto.response.CityOptionResponse;
import com.customermgmt.exception.GlobalExceptionHandler;
import com.customermgmt.service.CityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CityController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CityController MockMvc tests")
class CityControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CityService cityService;

    @Test
    @DisplayName("GET /search - returns matching city options")
    void searchCities_returnsMatches() throws Exception {
        CityOptionResponse colombo = CityOptionResponse.builder()
            .id(1L)
            .name("Colombo")
            .countryName("Sri Lanka")
            .build();
        CityOptionResponse colpetty = CityOptionResponse.builder()
            .id(2L)
            .name("Colpetty")
            .countryName("Sri Lanka")
            .build();

        when(cityService.searchCities("col"))
            .thenReturn(Arrays.asList(colombo, colpetty));

        mockMvc.perform(get("/api/v1/cities/search").param("q", "col"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("Colombo"))
            .andExpect(jsonPath("$.data[0].countryName").value("Sri Lanka"));
    }

    @Test
    @DisplayName("GET /search - returns empty list for blank query")
    void searchCities_blankQuery_returnsEmpty() throws Exception {
        when(cityService.searchCities("   ")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/cities/search").param("q", "   "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }
}
