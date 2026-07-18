package com.inn.cafe.service;

import com.inn.cafe.wrapper.RazorpayOrderResult;

/**
 * Real-time payment gateway integration (Razorpay), which itself supports UPI, Credit/Debit
 * Cards, Net Banking and Wallets inside a single hosted Checkout widget - so one integration
 * covers all the online payment methods the storefront offers. Cash on Delivery bypasses this
 * entirely and is handled directly in CartService.
 */
public interface RazorpayService {

    /**
     * Creates a real order against the Razorpay Orders API for the given amount (in rupees)
     * and returns everything the frontend needs to open the Checkout widget against it.
     */
    RazorpayOrderResult createOrder(double amountInRupees, String receipt);

    /**
     * Verifies the HMAC-SHA256 signature Razorpay returns to the frontend after a successful
     * payment, proving the payment_id/order_id pair genuinely came from Razorpay and wasn't
     * forged by a malicious client.
     */
    boolean verifySignature(String orderId, String paymentId, String signature);
}
