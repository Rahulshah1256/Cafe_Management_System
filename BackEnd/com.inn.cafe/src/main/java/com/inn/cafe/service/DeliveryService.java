package com.inn.cafe.service;

import com.inn.cafe.dto.DeliveryAssignRequest;
import com.inn.cafe.dto.DeliveryAvailabilityRequest;
import com.inn.cafe.dto.DeliveryPartnerRequest;
import com.inn.cafe.wrapper.DeliveryOrderWrapper;
import com.inn.cafe.wrapper.DeliveryPartnerWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DeliveryService {

    // Admin-only: onboard a new rider account.
    ResponseEntity<DeliveryPartnerWrapper> registerPartner(DeliveryPartnerRequest request);

    // Admin-only: list every delivery-partner account.
    ResponseEntity<List<DeliveryPartnerWrapper>> getAllPartners();

    // Admin-only: list only the riders currently AVAILABLE for a new assignment.
    ResponseEntity<List<DeliveryPartnerWrapper>> getAvailablePartners();

    // Delivery-partner-only: update the caller's own availability.
    ResponseEntity<DeliveryPartnerWrapper> updateAvailability(DeliveryAvailabilityRequest request);

    // Admin-only: assign a rider to an order.
    ResponseEntity<DeliveryOrderWrapper> assignPartner(Integer billId, DeliveryAssignRequest request);

    // Delivery-partner-only: the caller's own active deliveries, oldest first.
    ResponseEntity<List<DeliveryOrderWrapper>> getMyDeliveries();

    // Delivery-partner-only: mark one of the caller's assigned orders as DELIVERED.
    ResponseEntity<DeliveryOrderWrapper> completeDelivery(Integer billId);
}
