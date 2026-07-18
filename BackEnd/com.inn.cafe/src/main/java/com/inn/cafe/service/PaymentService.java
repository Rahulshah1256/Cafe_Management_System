package com.inn.cafe.service;

import com.inn.cafe.dto.VerifyPaymentRequest;
import com.inn.cafe.wrapper.PaymentStatusWrapper;
import org.springframework.http.ResponseEntity;

public interface PaymentService {

    ResponseEntity<PaymentStatusWrapper> getStatus(String uuid);

    ResponseEntity<PaymentStatusWrapper> retryPayment(String uuid);

    ResponseEntity<PaymentStatusWrapper> refund(String uuid);

    ResponseEntity<PaymentStatusWrapper> verifyPayment(VerifyPaymentRequest request);
}
