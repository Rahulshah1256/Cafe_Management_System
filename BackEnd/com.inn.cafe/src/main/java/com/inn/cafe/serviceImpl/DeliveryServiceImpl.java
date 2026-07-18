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
import com.inn.cafe.service.DeliveryService;
import com.inn.cafe.service.NotificationService;
import com.inn.cafe.wrapper.DeliveryOrderWrapper;
import com.inn.cafe.wrapper.DeliveryPartnerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    UserDao userDao;

    @Autowired
    BillDao billDao;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    NotificationService notificationService;

    @Override
    public ResponseEntity<DeliveryPartnerWrapper> registerPartner(DeliveryPartnerRequest request) {
        requireAdmin();
        if (Objects.nonNull(userDao.findByEmailId(request.getEmail()))) {
            throw new ValidationException("Email already exists: " + request.getEmail());
        }
        User partner = new User();
        partner.setName(request.getName());
        partner.setEmail(request.getEmail());
        partner.setPassword(passwordEncoder.encode(request.getPassword()));
        partner.setContactNumber(request.getContactNumber());
        partner.setVehicleNumber(request.getVehicleNumber());
        partner.setRole(CafeConstants.ROLE_DELIVERY_PARTNER);
        // Admin-created accounts are pre-approved (no "wait for admin approval" login gate).
        partner.setStatus("true");
        partner.setDeliveryAvailability(CafeConstants.DELIVERY_STATUS_OFFLINE);
        userDao.save(partner);
        log.info("Delivery partner registered: {}", partner.getEmail());
        return new ResponseEntity<>(toPartnerWrapper(partner), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<DeliveryPartnerWrapper>> getAllPartners() {
        requireAdmin();
        List<DeliveryPartnerWrapper> partners = userDao.findByRole(CafeConstants.ROLE_DELIVERY_PARTNER)
                .stream().map(this::toPartnerWrapper).toList();
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<DeliveryPartnerWrapper>> getAvailablePartners() {
        requireAdmin();
        List<DeliveryPartnerWrapper> partners = userDao.findByRoleAndDeliveryAvailability(
                        CafeConstants.ROLE_DELIVERY_PARTNER, CafeConstants.DELIVERY_STATUS_AVAILABLE)
                .stream().map(this::toPartnerWrapper).toList();
        return new ResponseEntity<>(partners, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeliveryPartnerWrapper> updateAvailability(DeliveryAvailabilityRequest request) {
        User partner = requireDeliveryPartner();
        String status = request.getStatus() == null ? null : request.getStatus().trim().toUpperCase();
        if (!CafeConstants.VALID_DELIVERY_AVAILABILITY_STATUSES.contains(status)) {
            throw new ValidationException("Invalid availability status: " + request.getStatus());
        }
        partner.setDeliveryAvailability(status);
        userDao.save(partner);
        return new ResponseEntity<>(toPartnerWrapper(partner), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeliveryOrderWrapper> assignPartner(Integer billId, DeliveryAssignRequest request) {
        requireAdmin();
        Bill bill = billDao.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
        if (CafeConstants.ORDER_STATUS_CANCELLED.equals(bill.getOrderStatus())
                || CafeConstants.ORDER_STATUS_DELIVERED.equals(bill.getOrderStatus())) {
            throw new ValidationException("Cannot assign a rider to an order that is already " + bill.getOrderStatus());
        }
        User partner = userDao.findByEmail(request.getPartnerEmail());
        if (partner == null || !CafeConstants.ROLE_DELIVERY_PARTNER.equalsIgnoreCase(partner.getRole())) {
            throw new ResourceNotFoundException("Delivery partner not found: " + request.getPartnerEmail());
        }
        bill.setAssignedDeliveryPartner(partner.getEmail());
        billDao.save(bill);

        partner.setDeliveryAvailability(CafeConstants.DELIVERY_STATUS_BUSY);
        userDao.save(partner);

        log.info("Bill {} assigned to delivery partner {}", bill.getUuid(), partner.getEmail());
        notificationService.notify(bill.getCreatedBy(), "Delivery Partner Assigned",
                "Your order " + bill.getUuid() + " has been assigned to a delivery partner.",
                "ORDER_STATUS", bill.getId());
        return new ResponseEntity<>(toOrderWrapper(bill), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<DeliveryOrderWrapper>> getMyDeliveries() {
        requireDeliveryPartner();
        List<DeliveryOrderWrapper> orders = billDao.findByAssignedDeliveryPartnerAndOrderStatusInOrderByCreatedAtAsc(
                        jwtFilter.getCurrentUsername(), CafeConstants.KITCHEN_QUEUE_STATUSES)
                .stream().map(this::toOrderWrapper).toList();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeliveryOrderWrapper> completeDelivery(Integer billId) {
        User partner = requireDeliveryPartner();
        Bill bill = billDao.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
        if (!partner.getEmail().equalsIgnoreCase(bill.getAssignedDeliveryPartner())) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        bill.setOrderStatus(CafeConstants.ORDER_STATUS_DELIVERED);
        bill.setDeliveredAt(Instant.now());
        billDao.save(bill);

        partner.setDeliveryAvailability(CafeConstants.DELIVERY_STATUS_AVAILABLE);
        userDao.save(partner);

        log.info("Bill {} marked DELIVERED by {}", bill.getUuid(), partner.getEmail());
        notificationService.notify(bill.getCreatedBy(), "Order Delivered",
                "Your order " + bill.getUuid() + " has been delivered. Enjoy your meal!",
                "ORDER_STATUS", bill.getId());
        return new ResponseEntity<>(toOrderWrapper(bill), HttpStatus.OK);
    }

    private void requireAdmin() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
    }

    private User requireDeliveryPartner() {
        if (!jwtFilter.isDeliveryPartner()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        User partner = userDao.findByEmail(jwtFilter.getCurrentUsername());
        if (partner == null) {
            throw new ResourceNotFoundException("Delivery partner account not found");
        }
        return partner;
    }

    private DeliveryPartnerWrapper toPartnerWrapper(User user) {
        return new DeliveryPartnerWrapper(user.getId(), user.getName(), user.getEmail(),
                user.getContactNumber(), user.getVehicleNumber(), user.getDeliveryAvailability());
    }

    private DeliveryOrderWrapper toOrderWrapper(Bill bill) {
        return new DeliveryOrderWrapper(bill.getId(), bill.getUuid(), bill.getName(), bill.getContactNumber(),
                bill.getDeliveryAddress(), bill.getOrderStatus(), bill.getTotal(), bill.getCreatedAt(),
                bill.getAssignedDeliveryPartner());
    }
}
