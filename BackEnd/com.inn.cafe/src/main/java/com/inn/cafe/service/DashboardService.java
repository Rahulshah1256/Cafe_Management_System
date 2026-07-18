package com.inn.cafe.service;

import com.inn.cafe.wrapper.KitchenOrderWrapper;
import com.inn.cafe.wrapper.SalesAnalyticsWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    ResponseEntity<Map<String, Object>> getCount();

    // Admin dashboard: revenue/order metrics (today/7d/30d), order-status breakdown, top sellers.
    ResponseEntity<SalesAnalyticsWrapper> getAnalytics();

    // Kitchen dashboard: live queue of in-progress orders, oldest first.
    ResponseEntity<List<KitchenOrderWrapper>> getKitchenQueue();
}
