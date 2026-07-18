package com.inn.cafe.rest;

import com.inn.cafe.wrapper.KitchenOrderWrapper;
import com.inn.cafe.wrapper.SalesAnalyticsWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/dashboard")
public interface DashboardRest {

    @GetMapping(path = "/details")
    public ResponseEntity<Map<String , Object>> getCount();

    // Admin-only: sales analytics (today/week/month revenue & orders, status breakdown, top sellers).
    @GetMapping(path = "/analytics")
    ResponseEntity<SalesAnalyticsWrapper> getAnalytics();

    // Admin/kitchen-facing: live queue of in-progress orders, oldest first.
    @GetMapping(path = "/kitchen-queue")
    ResponseEntity<List<KitchenOrderWrapper>> getKitchenQueue();

} 
