package com.customermgmt.service.impl;

import com.customermgmt.dto.response.CityOptionResponse;
import com.customermgmt.repository.CityRepository;
import com.customermgmt.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public List<CityOptionResponse> searchCities(String query) {
        String trimmedQuery = query == null ? "" : query.trim();
        if (trimmedQuery.isEmpty()) {
            return Collections.emptyList();
        }

        return cityRepository.searchByNameIgnoreCase(trimmedQuery).stream()
            .limit(10)
            .map(city -> CityOptionResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .countryName(city.getCountry().getName())
                .build())
            .collect(Collectors.toList());
    }
}
