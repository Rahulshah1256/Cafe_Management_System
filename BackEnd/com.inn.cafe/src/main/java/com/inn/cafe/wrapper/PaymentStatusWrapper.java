package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusWrapper {
    private String uuid;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private String orderStatus;
    private Integer total;
    private String message;

    // Populated only when a fresh Razorpay order was (re)created - e.g. on retryPayment() -
    // so the frontend can reopen the Checkout widget against it.
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Long razorpayAmount;
    private String razorpayCurrency;

    public PaymentStatusWrapper(String uuid, String paymentMethod, String paymentStatus, String transactionId,
                                 String orderStatus, Integer total, String message) {
        this(uuid, paymentMethod, paymentStatus, transactionId, orderStatus, total, message, null, null, null, null);
    }
}
