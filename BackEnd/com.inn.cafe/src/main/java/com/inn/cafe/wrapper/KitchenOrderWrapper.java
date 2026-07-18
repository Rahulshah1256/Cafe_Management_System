package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * A single order in the Kitchen Dashboard's live queue (orders that are
 * PLACED / ACCEPTED / PREPARING / OUT_FOR_DELIVERY), oldest first so kitchen
 * staff work through the queue in order-received sequence.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KitchenOrderWrapper {
    private Integer id;
    private String uuid;
    private String createdBy;
    private String contactNumber;
    private String orderStatus;
    private Integer total;
    private Instant createdAt;
    private List<KitchenOrderItemWrapper> items;
}
