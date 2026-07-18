package com.inn.cafe.rest;

import com.inn.cafe.dto.DeliveryAssignRequest;
import com.inn.cafe.dto.DeliveryAvailabilityRequest;
import com.inn.cafe.dto.DeliveryPartnerRequest;
import com.inn.cafe.wrapper.DeliveryOrderWrapper;
import com.inn.cafe.wrapper.DeliveryPartnerWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/delivery")
public interface DeliveryRest {

    @PostMapping(path = "/partners")
    ResponseEntity<DeliveryPartnerWrapper> registerPartner(@Valid @RequestBody DeliveryPartnerRequest request);

    @GetMapping(path = "/partners")
    ResponseEntity<List<DeliveryPartnerWrapper>> getAllPartners();

    @GetMapping(path = "/partners/available")
    ResponseEntity<List<DeliveryPartnerWrapper>> getAvailablePartners();

    @PutMapping(path = "/availability")
    ResponseEntity<DeliveryPartnerWrapper> updateAvailability(@Valid @RequestBody DeliveryAvailabilityRequest request);

    @PutMapping(path = "/assign/{billId}")
    ResponseEntity<DeliveryOrderWrapper> assignPartner(@PathVariable Integer billId,
                                                        @Valid @RequestBody DeliveryAssignRequest request);

    @GetMapping(path = "/my-deliveries")
    ResponseEntity<List<DeliveryOrderWrapper>> getMyDeliveries();

    @PutMapping(path = "/complete/{billId}")
    ResponseEntity<DeliveryOrderWrapper> completeDelivery(@PathVariable Integer billId);
}
