package com.customermgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressRequest {
    @NotBlank(message = "Address type is required")
    @Size(max = 50)
    private String type;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String addressLine1;
    private String addressLine2;

    @NotBlank(message = "Post code is required")
    @Size(max = 20)
    private String postCode;
    private Long cityId;
    private boolean primary;
}
