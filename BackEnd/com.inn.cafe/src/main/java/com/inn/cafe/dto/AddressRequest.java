package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    private String label;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private String landmark;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    // HOME, WORK, OTHER
    private String addressType = "HOME";

    private Boolean isDefault = Boolean.FALSE;
}
