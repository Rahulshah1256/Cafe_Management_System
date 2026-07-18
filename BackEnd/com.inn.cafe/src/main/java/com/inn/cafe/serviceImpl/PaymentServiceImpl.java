package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dto.VerifyPaymentRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.PaymentService;
import com.inn.cafe.service.RazorpayService;
import com.inn.cafe.wrapper.PaymentStatusWrapper;
import com.inn.cafe.wrapper.RazorpayOrderResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    BillDao billDao;

    @Autowired
    RazorpayService razorpayService;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    com.inn.cafe.service.NotificationService notificationService;

    @Override
    public ResponseEntity<PaymentStatusWrapper> getStatus(String uuid) {
        Bill bill = ownedBill(uuid);
        return new ResponseEntity<>(toWrapper(bill, null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> retryPayment(String uuid) {
        Bill bill = ownedBill(uuid);
        if (CafeConstants.PAYMENT_STATUS_SUCCESS.equals(bill.getPaymentStatus())) {
            throw new ValidationException("This order has already been paid for");
        }
        if (CafeConstants.isCashPaymentMethod(bill.getPaymentMethod())) {
            throw new ValidationException("Cash on delivery orders do not require online payment");
        }

        // Real-time gateway retry: create a fresh Razorpay order for the same bill so the
        // frontend can reopen the Checkout widget - payment status stays PENDING until the
        // customer completes payment and the frontend calls /payment/verify.
        RazorpayOrderResult order = razorpayService.createOrder(bill.getTotal(), bill.getUuid());
        bill.setRazorpayOrderId(order.getOrderId());
        bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_PENDING);
        billDao.save(bill);
        log.info("Payment retry for bill {}: new Razorpay order {} created", uuid, order.getOrderId());

        PaymentStatusWrapper wrapper = toWrapper(bill, "Payment gateway re-initiated, complete payment to confirm");
        wrapper.setRazorpayOrderId(order.getOrderId());
        wrapper.setRazorpayKeyId(order.getKeyId());
        wrapper.setRazorpayAmount(order.getAmountInPaise());
        wrapper.setRazorpayCurrency("INR");
        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> refund(String uuid) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Bill bill = billDao.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with uuid: " + uuid));

        if (!CafeConstants.PAYMENT_STATUS_SUCCESS.equals(bill.getPaymentStatus())) {
            throw new ValidationException("Only successfully paid orders can be refunded");
        }
        bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_REFUNDED);
        billDao.save(bill);
        log.info("Refund issued for bill {}", uuid);
        notificationService.notify(bill.getCreatedBy(), "Refund Issued",
                "A refund has been issued for your order " + bill.getUuid() + ".", "PAYMENT", bill.getId());
        return new ResponseEntity<>(toWrapper(bill, "Refund issued"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentStatusWrapper> verifyPayment(VerifyPaymentRequest request) {
        Bill bill = ownedBill(request.getBillUuid());
        if (CafeConstants.PAYMENT_STATUS_SUCCESS.equals(bill.getPaymentStatus())) {
            throw new ValidationException("This order has already been paid for");
        }
        if (bill.getRazorpayOrderId() == null || !bill.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {
            throw new ValidationException("This payment does not match the current order");
        }

        boolean valid = razorpayService.verifySignature(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());

        if (!valid) {
            bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_FAILED);
            billDao.save(bill);
            log.warn("Razorpay signature verification failed for bill {}", bill.getUuid());
            notificationService.notify(bill.getCreatedBy(), "Payment Failed",
                    "Your payment for order " + bill.getUuid() + " could not be verified. Please try again.",
                    "PAYMENT", bill.getId());
            throw new ValidationException("Payment verification failed. Please retry or contact support.");
        }

        bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_SUCCESS);
        bill.setTransactionId(request.getRazorpayPaymentId());
        billDao.save(bill);
        log.info("Payment verified for bill {}: transactionId={}", bill.getUuid(), request.getRazorpayPaymentId());
        notificationService.notify(bill.getCreatedBy(), "Payment Successful",
                "Your payment for order " + bill.getUuid() + " was successful.", "PAYMENT", bill.getId());
        return new ResponseEntity<>(toWrapper(bill, "Payment successful"), HttpStatus.OK);
    }

    private Bill ownedBill(String uuid) {
        Bill bill = billDao.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with uuid: " + uuid));
        if (!jwtFilter.isAdmin() && !jwtFilter.getCurrentUsername().equalsIgnoreCase(bill.getCreatedBy())) {
            throw new UnauthorizedException("This order does not belong to you");
        }
        return bill;
    }

    private PaymentStatusWrapper toWrapper(Bill bill, String message) {
        return new PaymentStatusWrapper(
                bill.getUuid(),
                bill.getPaymentMethod(),
                bill.getPaymentStatus(),
                bill.getTransactionId(),
                bill.getOrderStatus(),
                bill.getTotal(),
                message
        );
    }
}
