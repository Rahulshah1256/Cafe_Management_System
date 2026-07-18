package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Sales analytics summary for the Admin Dashboard: today/week(7d)/month(30d)
 * revenue & order counts, a breakdown of live orders by orderStatus, and the
 * top-selling products across all (non-cancelled) orders.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesAnalyticsWrapper {
    private long todayOrders;
    private double todayRevenue;
    private long weekOrders;
    private double weekRevenue;
    private long monthOrders;
    private double monthRevenue;
    private Map<String, Long> orderStatusCounts;
    private List<TopProductWrapper> topProducts;
}
