package com.inn.cafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request payload for POST /user/signup. Kept as a proper validated DTO (instead of the
 * generic Map<String,String> used elsewhere) since account creation is the most security
 * sensitive endpoint in the API.
 */
@Data
public class SignUpRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be a 10 digit number")
    private String contactNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ".{6,}", message = "Password must be at least 6 characters long")
    private String password;

    private String status;
}
