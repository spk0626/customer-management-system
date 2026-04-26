package com.customermgmt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "NIC number is required")
    @Size(max = 20)
    private String nicNumber;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Valid
    private List<CustomerMobileRequest> mobileNumbers;

    @Valid
    private List<CustomerAddressRequest> addresses;

    private List<Long> familyMemberIds;
}
