package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.DeliveryAssignRequest;
import com.inn.cafe.dto.DeliveryAvailabilityRequest;
import com.inn.cafe.dto.DeliveryPartnerRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.NotificationService;
import com.inn.cafe.wrapper.DeliveryOrderWrapper;
import com.inn.cafe.wrapper.DeliveryPartnerWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock private UserDao userDao;
    @Mock private BillDao billDao;
    @Mock private JwtFilter jwtFilter;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private User partner(String status) {
        User u = new User();
        u.setId(1);
        u.setEmail("rider@cafe.com");
        u.setRole(CafeConstants.ROLE_DELIVERY_PARTNER);
        u.setDeliveryAvailability(status);
        return u;
    }

    private Bill bill(String orderStatus, String assignedPartner) {
        Bill b = new Bill();
        b.setId(10);
        b.setUuid("BILL10");
        b.setOrderStatus(orderStatus);
        b.setAssignedDeliveryPartner(assignedPartner);
        b.setCreatedBy("user@cafe.com");
        return b;
    }

    @Test
    void registerPartner_shouldRejectNonAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        DeliveryPartnerRequest request = new DeliveryPartnerRequest();
        assertThrows(UnauthorizedException.class, () -> deliveryService.registerPartner(request));
    }

    @Test
    void registerPartner_shouldRejectDuplicateEmail() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        DeliveryPartnerRequest request = new DeliveryPartnerRequest();
        request.setEmail("rider@cafe.com");
        when(userDao.findByEmailId("rider@cafe.com")).thenReturn(partner(CafeConstants.DELIVERY_STATUS_OFFLINE));
        assertThrows(ValidationException.class, () -> deliveryService.registerPartner(request));
    }

    @Test
    void registerPartner_shouldSucceed_andDefaultToOffline() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        DeliveryPartnerRequest request = new DeliveryPartnerRequest();
        request.setName("Rider One");
        request.setEmail("rider@cafe.com");
        request.setPassword("password123");
        request.setContactNumber("9876543210");
        request.setVehicleNumber("KA01AB1234");
        when(userDao.findByEmailId("rider@cafe.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<DeliveryPartnerWrapper> response = deliveryService.registerPartner(request);

        assertEquals(CafeConstants.DELIVERY_STATUS_OFFLINE, response.getBody().getAvailability());
        assertEquals("KA01AB1234", response.getBody().getVehicleNumber());
    }

    @Test
    void updateAvailability_shouldRejectNonDeliveryPartner() {
        when(jwtFilter.isDeliveryPartner()).thenReturn(false);
        DeliveryAvailabilityRequest request = new DeliveryAvailabilityRequest();
        request.setStatus("AVAILABLE");
        assertThrows(UnauthorizedException.class, () -> deliveryService.updateAvailability(request));
    }

    @Test
    void updateAvailability_shouldRejectInvalidStatus() {
        when(jwtFilter.isDeliveryPartner()).thenReturn(true);
        when(jwtFilter.getCurrentUsername()).thenReturn("rider@cafe.com");
        when(userDao.findByEmail("rider@cafe.com")).thenReturn(partner(CafeConstants.DELIVERY_STATUS_OFFLINE));
        DeliveryAvailabilityRequest request = new DeliveryAvailabilityRequest();
        request.setStatus("ON_BREAK");
        assertThrows(ValidationException.class, () -> deliveryService.updateAvailability(request));
    }

    @Test
    void updateAvailability_shouldSucceed() {
        when(jwtFilter.isDeliveryPartner()).thenReturn(true);
        when(jwtFilter.getCurrentUsername()).thenReturn("rider@cafe.com");
        when(userDao.findByEmail("rider@cafe.com")).thenReturn(partner(CafeConstants.DELIVERY_STATUS_OFFLINE));
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        DeliveryAvailabilityRequest request = new DeliveryAvailabilityRequest();
        request.setStatus("available");

        ResponseEntity<DeliveryPartnerWrapper> response = deliveryService.updateAvailability(request);

        assertEquals(CafeConstants.DELIVERY_STATUS_AVAILABLE, response.getBody().getAvailability());
    }

    @Test
    void assignPartner_shouldRejectAlreadyDeliveredOrder() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findById(10)).thenReturn(Optional.of(bill(CafeConstants.ORDER_STATUS_DELIVERED, null)));
        DeliveryAssignRequest request = new DeliveryAssignRequest();
        request.setPartnerEmail("rider@cafe.com");
        assertThrows(ValidationException.class, () -> deliveryService.assignPartner(10, request));
    }

    @Test
    void assignPartner_shouldRejectUnknownOrNonDeliveryEmail() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(billDao.findById(10)).thenReturn(Optional.of(bill(CafeConstants.ORDER_STATUS_PLACED, null)));
        when(userDao.findByEmail("not-a-rider@cafe.com")).thenReturn(null);
        DeliveryAssignRequest request = new DeliveryAssignRequest();
        request.setPartnerEmail("not-a-rider@cafe.com");
        assertThrows(ResourceNotFoundException.class, () -> deliveryService.assignPartner(10, request));
    }

    @Test
    void assignPartner_shouldSucceed_andMarkPartnerBusy() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Bill b = bill(CafeConstants.ORDER_STATUS_PREPARING, null);
        when(billDao.findById(10)).thenReturn(Optional.of(b));
        User rider = partner(CafeConstants.DELIVERY_STATUS_AVAILABLE);
        when(userDao.findByEmail("rider@cafe.com")).thenReturn(rider);
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        DeliveryAssignRequest request = new DeliveryAssignRequest();
        request.setPartnerEmail("rider@cafe.com");

        ResponseEntity<DeliveryOrderWrapper> response = deliveryService.assignPartner(10, request);

        assertEquals("rider@cafe.com", response.getBody().getAssignedDeliveryPartner());
        assertEquals(CafeConstants.DELIVERY_STATUS_BUSY, rider.getDeliveryAvailability());
    }

    @Test
    void completeDelivery_shouldRejectWhenNotAssignedPartner() {
        when(jwtFilter.isDeliveryPartner()).thenReturn(true);
        when(jwtFilter.getCurrentUsername()).thenReturn("rider@cafe.com");
        when(userDao.findByEmail("rider@cafe.com")).thenReturn(partner(CafeConstants.DELIVERY_STATUS_BUSY));
        when(billDao.findById(10)).thenReturn(Optional.of(bill(CafeConstants.ORDER_STATUS_OUT_FOR_DELIVERY, "other@cafe.com")));
        assertThrows(UnauthorizedException.class, () -> deliveryService.completeDelivery(10));
    }

    @Test
    void completeDelivery_shouldSucceed_andFreeUpPartner() {
        when(jwtFilter.isDeliveryPartner()).thenReturn(true);
        when(jwtFilter.getCurrentUsername()).thenReturn("rider@cafe.com");
        User rider = partner(CafeConstants.DELIVERY_STATUS_BUSY);
        when(userDao.findByEmail("rider@cafe.com")).thenReturn(rider);
        Bill b = bill(CafeConstants.ORDER_STATUS_OUT_FOR_DELIVERY, "rider@cafe.com");
        when(billDao.findById(10)).thenReturn(Optional.of(b));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<DeliveryOrderWrapper> response = deliveryService.completeDelivery(10);

        assertEquals(CafeConstants.ORDER_STATUS_DELIVERED, response.getBody().getOrderStatus());
        assertEquals(CafeConstants.DELIVERY_STATUS_AVAILABLE, rider.getDeliveryAvailability());
    }
}
