package com.inn.cafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request payload for POST /user/verifySignupOtp - completes the two-step signup flow
 * started by POST /user/signup, by confirming the 6-digit OTP emailed to the address.
 */
@Data
public class VerifySignupOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6 digit code")
    private String otp;
}
