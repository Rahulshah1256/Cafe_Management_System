package com.inn.cafe.restImpl;

import com.inn.cafe.rest.DashboardRest;
import com.inn.cafe.service.DashboardService;
import com.inn.cafe.wrapper.KitchenOrderWrapper;
import com.inn.cafe.wrapper.SalesAnalyticsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DashboardRestImpl implements DashboardRest {

    @Autowired
    DashboardService dashboardService;
    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        return dashboardService.getCount();
    }

    @Override
    public ResponseEntity<SalesAnalyticsWrapper> getAnalytics() {
        return dashboardService.getAnalytics();
    }

    @Override
    public ResponseEntity<List<KitchenOrderWrapper>> getKitchenQueue() {
        return dashboardService.getKitchenQueue();
    }
}
