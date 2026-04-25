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
public class CustomerMobileRequest {
    @NotBlank(message = "Mobile number is required")
    @Size(max = 20)
    private String mobileNumber;
    private boolean primary;
}
