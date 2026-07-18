package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A single order as seen by an admin (assignment view) or a delivery partner (their own
 * queue) - carries the delivery-relevant fields only (not full item lines/payment details,
 * which the rider doesn't need).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryOrderWrapper {
    private Integer billId;
    private String uuid;
    private String customerName;
    private String contactNumber;
    private String deliveryAddress;
    private String orderStatus;
    private Integer total;
    private Instant createdAt;
    private String assignedDeliveryPartner;
}
