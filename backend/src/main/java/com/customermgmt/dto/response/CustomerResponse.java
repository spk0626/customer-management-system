package com.customermgmt.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String nicNumber;
    private boolean active;
    private List<MobileResponse> mobileNumbers;
    private List<AddressResponse> addresses;
    private List<FamilyMemberResponse> familyMembers;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MobileResponse {
        private Long id;
        private String mobileNumber;
        private boolean primary;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddressResponse {
        private Long id;
        private String addressLine1;
        private String addressLine2;
        private String cityName;
        private String countryName;
        private boolean primary;
    }

    /** Shallow family member — avoids recursive nesting */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FamilyMemberResponse {
        private Long id;
        private String name;
        private String nicNumber;
    }
}
