package com.customermgmt.mapper;

import com.customermgmt.dto.response.CustomerResponse;
import com.customermgmt.entity.Customer;
import com.customermgmt.entity.CustomerAddress;
import com.customermgmt.entity.CustomerMobile;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    // -------------------------------------------------------------------------
    // Qualifiers — tell MapStruct which Customer→CustomerResponse method
    // to use when mapping list elements. Without these, MapStruct sees two
    // methods with the same source/target type and throws "Ambiguous mapping".
    // -------------------------------------------------------------------------

    @Qualifier
    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @interface FullMapping {}

    @Qualifier
    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @interface ListMapping {}

    // -------------------------------------------------------------------------
    // Single-entity mappings
    // -------------------------------------------------------------------------

    /**
     * FULL response — all collections included.
     * Only call when familyMembers + mobiles + addresses are eagerly loaded.
     */
    @FullMapping
    @Mapping(target = "mobileCount", ignore = true)
    @Mapping(target = "addressCount", ignore = true)
    @Mapping(target = "familyMemberCount", ignore = true)
    @Mapping(target = "mobileNumbers", source = "mobileNumbers")
    @Mapping(target = "addresses",     source = "addresses")
    @Mapping(target = "familyMembers", source = "familyMembers")
    CustomerResponse toResponse(Customer customer);

    /**
     * LIST response — scalar fields only.
     * Safe to call on detached entities — never touches lazy collections.
     */
    @ListMapping
    @Mapping(target = "mobileCount", ignore = true)
    @Mapping(target = "addressCount", ignore = true)
    @Mapping(target = "familyMemberCount", ignore = true)
    @Mapping(target = "mobileNumbers", ignore = true)
    @Mapping(target = "addresses",     ignore = true)
    @Mapping(target = "familyMembers", ignore = true)
    CustomerResponse toListResponse(Customer customer);

    // -------------------------------------------------------------------------
    // Child mappings
    // -------------------------------------------------------------------------

    @Mapping(target = "cityName",    source = "city.name")
    @Mapping(target = "countryName", source = "city.country.name")
    CustomerResponse.AddressResponse toAddressResponse(CustomerAddress address);

    @Mapping(target = "id",           source = "id")
    @Mapping(target = "mobileNumber", source = "mobileNumber")
    @Mapping(target = "primary",      source = "primary")
    CustomerResponse.MobileResponse toMobileResponse(CustomerMobile mobile);

    // -------------------------------------------------------------------------
    // List mappings — qualifier tells MapStruct which element method to use
    // -------------------------------------------------------------------------

    /**
     * Full list — for create/update responses where all data is loaded.
     */
    @IterableMapping(qualifiedBy = FullMapping.class)
    List<CustomerResponse> toResponseList(List<Customer> customers);

    /**
     * Shallow list — for paginated table view and search results.
     * Uses toListResponse so no lazy collections are accessed.
     */
    @IterableMapping(qualifiedBy = ListMapping.class)
    List<CustomerResponse> toListResponseList(List<Customer> customers);

    // -------------------------------------------------------------------------
    // Family member mapping
    // -------------------------------------------------------------------------

    default List<CustomerResponse.FamilyMemberResponse> toFamilyMemberResponses(Set<Customer> members) {
        if (members == null) return Collections.emptyList();
        return members.stream()
            .map(m -> CustomerResponse.FamilyMemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .nicNumber(m.getNicNumber())
                .build())
            .collect(Collectors.toList());
    }
}
