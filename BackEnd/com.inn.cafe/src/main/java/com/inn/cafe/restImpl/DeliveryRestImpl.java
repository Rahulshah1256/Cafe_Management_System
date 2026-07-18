package com.inn.cafe.restImpl;

import com.inn.cafe.dto.DeliveryAssignRequest;
import com.inn.cafe.dto.DeliveryAvailabilityRequest;
import com.inn.cafe.dto.DeliveryPartnerRequest;
import com.inn.cafe.rest.DeliveryRest;
import com.inn.cafe.service.DeliveryService;
import com.inn.cafe.wrapper.DeliveryOrderWrapper;
import com.inn.cafe.wrapper.DeliveryPartnerWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeliveryRestImpl implements DeliveryRest {

    @Autowired
    DeliveryService deliveryService;

    @Override
    public ResponseEntity<DeliveryPartnerWrapper> registerPartner(DeliveryPartnerRequest request) {
        return deliveryService.registerPartner(request);
    }

    @Override
    public ResponseEntity<List<DeliveryPartnerWrapper>> getAllPartners() {
        return deliveryService.getAllPartners();
    }

    @Override
    public ResponseEntity<List<DeliveryPartnerWrapper>> getAvailablePartners() {
        return deliveryService.getAvailablePartners();
    }

    @Override
    public ResponseEntity<DeliveryPartnerWrapper> updateAvailability(DeliveryAvailabilityRequest request) {
        return deliveryService.updateAvailability(request);
    }

    @Override
    public ResponseEntity<DeliveryOrderWrapper> assignPartner(Integer billId, DeliveryAssignRequest request) {
        return deliveryService.assignPartner(billId, request);
    }

    @Override
    public ResponseEntity<List<DeliveryOrderWrapper>> getMyDeliveries() {
        return deliveryService.getMyDeliveries();
    }

    @Override
    public ResponseEntity<DeliveryOrderWrapper> completeDelivery(Integer billId) {
        return deliveryService.completeDelivery(billId);
    }
}
