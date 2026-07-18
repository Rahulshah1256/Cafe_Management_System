package com.inn.cafe.serviceImpl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.service.DashboardService;
import com.inn.cafe.wrapper.KitchenOrderItemWrapper;
import com.inn.cafe.wrapper.KitchenOrderWrapper;
import com.inn.cafe.wrapper.SalesAnalyticsWrapper;
import com.inn.cafe.wrapper.TopProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    productDao productDao;

    @Autowired
    BillDao billDao;

    @Autowired
    JwtFilter jwtFilter;

    private static final Gson GSON = new Gson();


    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        System.out.println("inside getCount");

        Map<String , Object> map = new HashMap<>();
        map.put("category" , categoryDao.count());
        map.put("product" , productDao.count());
        map.put("bill" , billDao.count());
        return new ResponseEntity<>(map , HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SalesAnalyticsWrapper> getAnalytics() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }

        List<Bill> allBills = billDao.getAllBills();

        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfWeek = startOfToday.minus(java.time.Duration.ofDays(6));
        Instant startOfMonth = startOfToday.minus(java.time.Duration.ofDays(29));

        long todayOrders = 0, weekOrders = 0, monthOrders = 0;
        double todayRevenue = 0, weekRevenue = 0, monthRevenue = 0;
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (String status : CafeConstants.VALID_ORDER_STATUSES) {
            statusCounts.put(status, 0L);
        }
        Map<String, TopProductWrapper> productTotals = new LinkedHashMap<>();

        for (Bill bill : allBills) {
            String status = bill.getOrderStatus();
            if (status != null) {
                statusCounts.merge(status, 1L, Long::sum);
            }

            boolean cancelled = CafeConstants.ORDER_STATUS_CANCELLED.equals(status);
            double total = bill.getTotal() == null ? 0 : bill.getTotal();
            Instant createdAt = bill.getCreatedAt();

            if (!cancelled && createdAt != null) {
                if (!createdAt.isBefore(startOfToday)) {
                    todayOrders++;
                    todayRevenue += total;
                }
                if (!createdAt.isBefore(startOfWeek)) {
                    weekOrders++;
                    weekRevenue += total;
                }
                if (!createdAt.isBefore(startOfMonth)) {
                    monthOrders++;
                    monthRevenue += total;
                }
            }

            if (!cancelled) {
                accumulateProductTotals(bill, productTotals);
            }
        }

        List<TopProductWrapper> topProducts = new ArrayList<>(productTotals.values());
        topProducts.sort(Comparator.comparing(TopProductWrapper::getQuantitySold).reversed());
        if (topProducts.size() > 5) {
            topProducts = topProducts.subList(0, 5);
        }

        SalesAnalyticsWrapper wrapper = new SalesAnalyticsWrapper(
                todayOrders, todayRevenue, weekOrders, weekRevenue, monthOrders, monthRevenue,
                statusCounts, topProducts);
        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<KitchenOrderWrapper>> getKitchenQueue() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }

        List<Bill> bills = billDao.findByOrderStatusInOrderByCreatedAtAsc(CafeConstants.KITCHEN_QUEUE_STATUSES);
        List<KitchenOrderWrapper> queue = new ArrayList<>();
        for (Bill bill : bills) {
            List<KitchenOrderItemWrapper> items = new ArrayList<>();
            for (Map<String, Object> line : parseInvoiceLines(bill)) {
                if (isProductLine(line)) {
                    items.add(new KitchenOrderItemWrapper(
                            String.valueOf(line.get("name")), String.valueOf(line.get("quantity"))));
                }
            }
            queue.add(new KitchenOrderWrapper(bill.getId(), bill.getUuid(), bill.getCreatedBy(),
                    bill.getContactNumber(), bill.getOrderStatus(), bill.getTotal(), bill.getCreatedAt(), items));
        }
        return new ResponseEntity<>(queue, HttpStatus.OK);
    }

    private void accumulateProductTotals(Bill bill, Map<String, TopProductWrapper> productTotals) {
        for (Map<String, Object> line : parseInvoiceLines(bill)) {
            if (!isProductLine(line)) {
                continue;
            }
            String name = String.valueOf(line.get("name"));
            int quantity = parseIntSafely(line.get("quantity"));
            double lineTotal = parseDoubleSafely(line.get("total"));

            TopProductWrapper existing = productTotals.get(name);
            if (existing == null) {
                productTotals.put(name, new TopProductWrapper(name, quantity, lineTotal));
            } else {
                existing.setQuantitySold(existing.getQuantitySold() + quantity);
                existing.setRevenue(existing.getRevenue() + lineTotal);
            }
        }
    }

    private boolean isProductLine(Map<String, Object> line) {
        Object category = line.get("category");
        return category == null || !CafeConstants.INVOICE_LINE_CHARGES_CATEGORY.equals(category);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseInvoiceLines(Bill bill) {
        if (bill.getProductDetails() == null || bill.getProductDetails().isBlank()) {
            return List.of();
        }
        try {
            return GSON.fromJson(bill.getProductDetails(), new TypeToken<List<Map<String, Object>>>() {
            }.getType());
        } catch (Exception e) {
            log.warn("Could not parse productDetails for bill {}: {}", bill.getId(), e.getMessage());
            return List.of();
        }
    }

    private int parseIntSafely(Object value) {
        try {
            return value == null ? 0 : (int) Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSafely(Object value) {
        try {
            return value == null ? 0 : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
