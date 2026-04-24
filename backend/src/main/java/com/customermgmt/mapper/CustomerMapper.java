package com.customermgmt.mapper;

import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.entity.Customer;
import com.customermgmt.entity.CustomerAddress;
import com.customermgmt.entity.CustomerMobile;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "mobileNumbers", source = "mobileNumbers")
    @Mapping(target = "addresses", source = "addresses")
    @Mapping(target = "familyMembers", source = "familyMembers")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "mobileNumber", source = "mobileNumber")
    @Mapping(target = "primary", source = "primary")
    CustomerResponse.MobileResponse toMobileResponse(CustomerMobile mobile);

    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "countryName", source = "city.country.name")
    CustomerResponse.AddressResponse toAddressResponse(CustomerAddress address);

    List<CustomerResponse> toResponseList(List<Customer> customers);

    default List<CustomerResponse.FamilyMemberResponse> toFamilyMemberResponses(Set<Customer> members) {
        if (members == null) return null;
        return members.stream()
            .map(m -> CustomerResponse.FamilyMemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .nicNumber(m.getNicNumber())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}