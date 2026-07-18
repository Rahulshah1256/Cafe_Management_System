package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.RazorpayService;
import com.inn.cafe.wrapper.RazorpayOrderResult;
import com.inn.cafe.wrapper.PaymentStatusWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private BillDao billDao;
    @Mock private RazorpayService razorpayService;
    @Mock private JwtFilter jwtFilter;
    @Mock private com.inn.cafe.service.NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Bill bill(String paymentStatus, String paymentMethod, String createdBy) {
        Bill bill = new Bill();
        bill.setUuid("BILL123");
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentStatus(paymentStatus);
        bill.setOrderStatus(CafeConstants.ORDER_STATUS_PLACED);
        bill.setTotal(500);
        bill.setCreatedBy(createdBy);
        return bill;
    }

    @Test
    void getStatus_shouldReturnBill_whenOwnedByCurrentUser() {
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill(CafeConstants.PAYMENT_STATUS_SUCCESS, "UPI", "user@cafe.com")));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        ResponseEntity<PaymentStatusWrapper> response = paymentService.getStatus("BILL123");

        assertEquals(CafeConstants.PAYMENT_STATUS_SUCCESS, response.getBody().getPaymentStatus());
    }

    @Test
    void getStatus_shouldThrowUnauthorized_whenBillBelongsToAnotherUser() {
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill(CafeConstants.PAYMENT_STATUS_SUCCESS, "UPI", "other@cafe.com")));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(UnauthorizedException.class, () -> paymentService.getStatus("BILL123"));
    }

    @Test
    void getStatus_shouldThrowNotFound_whenUuidUnknown() {
        when(billDao.findByUuid("MISSING")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getStatus("MISSING"));
    }

    @Test
    void retryPayment_shouldCreateFreshRazorpayOrder_andStayPending() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_FAILED, "UPI", "user@cafe.com");
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(razorpayService.createOrder(anyDouble(), eq("BILL123")))
                .thenReturn(new RazorpayOrderResult("order_NEW999", "rzp_test_key", 50000L, "INR"));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<PaymentStatusWrapper> response = paymentService.retryPayment("BILL123");

        assertEquals(CafeConstants.PAYMENT_STATUS_PENDING, response.getBody().getPaymentStatus());
        assertEquals("order_NEW999", response.getBody().getRazorpayOrderId());
        assertEquals("rzp_test_key", response.getBody().getRazorpayKeyId());
        assertEquals(50000L, response.getBody().getRazorpayAmount());
    }

    @Test
    void retryPayment_shouldThrowValidation_whenAlreadyPaid() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_SUCCESS, "UPI", "user@cafe.com");
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(ValidationException.class, () -> paymentService.retryPayment("BILL123"));
    }

    @Test
    void retryPayment_shouldThrowValidation_whenCashOnDelivery() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_PENDING, "COD", "user@cafe.com");
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(ValidationException.class, () -> paymentService.retryPayment("BILL123"));
    }

    @Test
    void verifyPayment_shouldMarkSuccess_whenSignatureValid() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_PENDING, "UPI", "user@cafe.com");
        bill.setRazorpayOrderId("order_ABC123");
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(razorpayService.verifySignature("order_ABC123", "pay_XYZ", "sig123")).thenReturn(true);
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        com.inn.cafe.dto.VerifyPaymentRequest request = new com.inn.cafe.dto.VerifyPaymentRequest();
        request.setBillUuid("BILL123");
        request.setRazorpayOrderId("order_ABC123");
        request.setRazorpayPaymentId("pay_XYZ");
        request.setRazorpaySignature("sig123");

        ResponseEntity<PaymentStatusWrapper> response = paymentService.verifyPayment(request);

        assertEquals(CafeConstants.PAYMENT_STATUS_SUCCESS, response.getBody().getPaymentStatus());
        assertEquals("pay_XYZ", response.getBody().getTransactionId());
    }

    @Test
    void verifyPayment_shouldThrowValidation_whenOrderIdMismatch() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_PENDING, "UPI", "user@cafe.com");
        bill.setRazorpayOrderId("order_ABC123");
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        com.inn.cafe.dto.VerifyPaymentRequest request = new com.inn.cafe.dto.VerifyPaymentRequest();
        request.setBillUuid("BILL123");
        request.setRazorpayOrderId("order_DIFFERENT");
        request.setRazorpayPaymentId("pay_XYZ");
        request.setRazorpaySignature("sig123");

        assertThrows(ValidationException.class, () -> paymentService.verifyPayment(request));
    }

    @Test
    void refund_shouldThrowUnauthorized_whenNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> paymentService.refund("BILL123"));
    }

    @Test
    void refund_shouldMarkRefunded_whenAdminAndPaid() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_SUCCESS, "UPI", "user@cafe.com");
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<PaymentStatusWrapper> response = paymentService.refund("BILL123");

        assertEquals(CafeConstants.PAYMENT_STATUS_REFUNDED, response.getBody().getPaymentStatus());
    }

    @Test
    void refund_shouldThrowValidation_whenNotYetPaid() {
        Bill bill = bill(CafeConstants.PAYMENT_STATUS_PENDING, "COD", "user@cafe.com");
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findByUuid("BILL123")).thenReturn(Optional.of(bill));

        assertThrows(ValidationException.class, () -> paymentService.refund("BILL123"));
    }
}
