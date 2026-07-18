package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for POST /payment/verify - sent by the frontend after the Razorpay
 * Checkout widget's success handler fires, so the backend can cryptographically verify the
 * payment genuinely completed before marking the order paid.
 */
@Data
public class VerifyPaymentRequest {

    @NotBlank(message = "Bill uuid is required")
    private String billUuid;

    @NotBlank(message = "Razorpay order id is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
