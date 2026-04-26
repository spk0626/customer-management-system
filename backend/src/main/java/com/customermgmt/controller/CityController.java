package com.customermgmt.controller;

import com.customermgmt.dto.response.ApiResponse;
import com.customermgmt.dto.response.CityOptionResponse;
import com.customermgmt.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/search")
    public ApiResponse<List<CityOptionResponse>> searchCities(@RequestParam String q) {
        return ApiResponse.ok(cityService.searchCities(q));
    }
}
