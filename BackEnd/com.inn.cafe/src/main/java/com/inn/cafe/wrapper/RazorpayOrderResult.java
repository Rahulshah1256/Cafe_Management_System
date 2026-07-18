package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details the frontend needs to open the Razorpay Checkout widget for a freshly
 * created order (amount is in the smallest currency unit - paise - as Razorpay expects).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResult {
    private String orderId;
    private String keyId;
    private long amountInPaise;
    private String currency;
}
