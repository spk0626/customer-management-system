package com.customermgmt.service;

import com.customermgmt.dto.response.CityOptionResponse;

import java.util.List;

public interface CityService {

    List<CityOptionResponse> searchCities(String query);
}
