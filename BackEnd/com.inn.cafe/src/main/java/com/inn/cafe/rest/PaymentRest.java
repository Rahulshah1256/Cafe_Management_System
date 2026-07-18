package com.inn.cafe.rest;

import com.inn.cafe.dto.VerifyPaymentRequest;
import com.inn.cafe.wrapper.PaymentStatusWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/payment")
public interface PaymentRest {

    @GetMapping(path = "/status/{uuid}")
    ResponseEntity<PaymentStatusWrapper> getStatus(@PathVariable String uuid);

    @PostMapping(path = "/retry/{uuid}")
    ResponseEntity<PaymentStatusWrapper> retryPayment(@PathVariable String uuid);

    @PutMapping(path = "/refund/{uuid}")
    ResponseEntity<PaymentStatusWrapper> refund(@PathVariable String uuid);

    @PostMapping(path = "/verify")
    ResponseEntity<PaymentStatusWrapper> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request);
}
