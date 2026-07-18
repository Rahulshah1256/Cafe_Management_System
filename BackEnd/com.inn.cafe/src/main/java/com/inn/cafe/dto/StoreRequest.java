package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "Store name is required")
    private String name;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private Double latitude;

    private Double longitude;

    private String phone;

    private String openingHours;

    private Boolean active = Boolean.TRUE;
}
