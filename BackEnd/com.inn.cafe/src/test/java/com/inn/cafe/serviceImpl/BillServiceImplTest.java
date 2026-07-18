package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.OrderStatusUpdateRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock private BillDao billDao;
    @Mock private JwtFilter jwtFilter;
    @Mock private UserDao userDao;
    @Mock private com.inn.cafe.service.NotificationService notificationService;

    @InjectMocks
    private BillServiceImpl billService;

    private Bill bill(String orderStatus, String createdBy) {
        Bill bill = new Bill();
        bill.setId(1);
        bill.setUuid("BILL123");
        bill.setOrderStatus(orderStatus);
        bill.setCreatedBy(createdBy);
        return bill;
    }

    @Test
    void cancelOrder_shouldSucceed_whenOwnerAndPlaced() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PLACED, "user@cafe.com");
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Bill> response = billService.cancelOrder(1);

        assertEquals(CafeConstants.ORDER_STATUS_CANCELLED, response.getBody().getOrderStatus());
    }

    @Test
    void cancelOrder_shouldSucceed_whenAdmin_evenIfNotOwner() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_ACCEPTED, "user@cafe.com");
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Bill> response = billService.cancelOrder(1);

        assertEquals(CafeConstants.ORDER_STATUS_CANCELLED, response.getBody().getOrderStatus());
    }

    @Test
    void cancelOrder_shouldReverseLoyaltyPoints() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PLACED, "user@cafe.com");
        bill.setLoyaltyPointsEarned(55);
        bill.setLoyaltyPointsRedeemed(100);
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = new User();
        user.setEmail("user@cafe.com");
        user.setLoyaltyPoints(55); // balance after the original checkout (100 redeemed, 55 earned, starting from 100)
        when(userDao.findByEmail("user@cafe.com")).thenReturn(user);

        billService.cancelOrder(1);

        // reversal: 55 (current) - 55 (earned) + 100 (redeemed) = 100
        assertEquals(100, user.getLoyaltyPoints());
        verify(userDao).save(user);
    }

    @Test
    void cancelOrder_shouldThrowUnauthorized_whenNotOwnerNorAdmin() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PLACED, "other@cafe.com");
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(UnauthorizedException.class, () -> billService.cancelOrder(1));
    }

    @Test
    void cancelOrder_shouldThrowValidation_whenAlreadyPreparing() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PREPARING, "user@cafe.com");
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(ValidationException.class, () -> billService.cancelOrder(1));
    }

    @Test
    void cancelOrder_shouldThrowNotFound_whenBillMissing() {
        when(billDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.cancelOrder(99));
    }

    @Test
    void updateOrderStatus_shouldThrowUnauthorized_whenNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus("PREPARING");

        assertThrows(UnauthorizedException.class, () -> billService.updateOrderStatus(1, request));
    }

    @Test
    void updateOrderStatus_shouldThrowValidation_whenStatusInvalid() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PLACED, "user@cafe.com");
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findById(1)).thenReturn(Optional.of(bill));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus("BOGUS");

        assertThrows(ValidationException.class, () -> billService.updateOrderStatus(1, request));
    }

    @Test
    void updateOrderStatus_shouldSucceed_whenAdminAndValidStatus() {
        Bill bill = bill(CafeConstants.ORDER_STATUS_PLACED, "user@cafe.com");
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findById(1)).thenReturn(Optional.of(bill));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus("preparing");

        ResponseEntity<Bill> response = billService.updateOrderStatus(1, request);

        assertEquals(CafeConstants.ORDER_STATUS_PREPARING, response.getBody().getOrderStatus());
    }
}
