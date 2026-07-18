package com.inn.cafe.restImpl;

import com.inn.cafe.dto.VerifyPaymentRequest;
import com.inn.cafe.rest.PaymentRest;
import com.inn.cafe.service.PaymentService;
import com.inn.cafe.wrapper.PaymentStatusWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentRestImpl implements PaymentRest {

    @Autowired
    PaymentService paymentService;

    @Override
    public ResponseEntity<PaymentStatusWrapper> getStatus(String uuid) {
        return paymentService.getStatus(uuid);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> retryPayment(String uuid) {
        return paymentService.retryPayment(uuid);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> refund(String uuid) {
        return paymentService.refund(uuid);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> verifyPayment(VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request);
    }
}
