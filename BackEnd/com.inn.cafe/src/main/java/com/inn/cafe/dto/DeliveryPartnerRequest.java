package com.inn.cafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Admin-only request to onboard a new delivery partner (rider) account. There is no public
 * self-signup for this role - only admins create delivery-partner logins, mirroring how a
 * real food-delivery platform vets its riders before granting them access.
 */
@Data
public class DeliveryPartnerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ".{6,}", message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be a 10 digit number")
    private String contactNumber;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;
}
