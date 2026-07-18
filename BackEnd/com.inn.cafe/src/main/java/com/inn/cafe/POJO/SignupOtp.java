package com.inn.cafe.POJO;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Holds a pending (not-yet-verified) signup while the user completes email OTP
 * verification. A real User row is only created once the correct OTP is confirmed via
 * POST /user/verifySignupOtp - this keeps the "user" table free of unverified/abandoned
 * signups and doubles as the natural place to store the OTP's hash/expiry/attempt count.
 */
@Entity
@Data
@Table(name = "signup_otp")
public class SignupOtp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "contact_number")
    private String contactNumber;

    // Already-hashed (BCrypt) password for the account that will be created on success -
    // never store or email the plaintext password.
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "requested_status")
    private String requestedStatus;

    // Hashed (BCrypt) OTP - never stored or logged in plaintext.
    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
