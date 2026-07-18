package com.inn.cafe.serviceImpl;

import com.google.gson.Gson;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.wrapper.KitchenOrderWrapper;
import com.inn.cafe.wrapper.SalesAnalyticsWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private CategoryDao categoryDao;
    @Mock private productDao productDao;
    @Mock private BillDao billDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private static final Gson GSON = new Gson();

    private String invoiceJson(String productName, int quantity, double lineTotal) {
        Map<String, Object> productLine = new LinkedHashMap<>();
        productLine.put("name", productName);
        productLine.put("category", "Pizza");
        productLine.put("quantity", String.valueOf(quantity));
        productLine.put("price", lineTotal / quantity);
        productLine.put("total", lineTotal);

        Map<String, Object> chargeLine = new LinkedHashMap<>();
        chargeLine.put("name", "Delivery Charge");
        chargeLine.put("category", "Charges");
        chargeLine.put("quantity", "1");
        chargeLine.put("price", 40.0);
        chargeLine.put("total", 40.0);

        return GSON.toJson(List.of(productLine, chargeLine));
    }

    private Bill bill(Integer total, String orderStatus, Instant createdAt, String productJson) {
        Bill bill = new Bill();
        bill.setId(1);
        bill.setUuid("BILL-" + total);
        bill.setTotal(total);
        bill.setOrderStatus(orderStatus);
        bill.setCreatedAt(createdAt);
        bill.setProductDetails(productJson);
        return bill;
    }

    @Test
    void getAnalytics_shouldRejectNonAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> dashboardService.getAnalytics());
    }

    @Test
    void getAnalytics_shouldAggregateRevenueAndTopProducts_excludingCancelledOrders() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Instant now = Instant.now();
        Bill todayDelivered = bill(500, CafeConstants.ORDER_STATUS_DELIVERED, now,
                invoiceJson("Margherita Pizza", 2, 460.0));
        Bill todayCancelled = bill(300, CafeConstants.ORDER_STATUS_CANCELLED, now,
                invoiceJson("Farmhouse Pizza", 1, 260.0));
        Bill lastWeek = bill(200, CafeConstants.ORDER_STATUS_DELIVERED, now.minus(3, ChronoUnit.DAYS),
                invoiceJson("Margherita Pizza", 1, 230.0));
        when(billDao.getAllBills()).thenReturn(List.of(todayDelivered, todayCancelled, lastWeek));

        ResponseEntity<SalesAnalyticsWrapper> response = dashboardService.getAnalytics();
        SalesAnalyticsWrapper body = response.getBody();

        assertNotNull(body);
        // only the non-cancelled today order counts
        assertEquals(1, body.getTodayOrders());
        assertEquals(500.0, body.getTodayRevenue());
        // today + last week non-cancelled orders count toward the week window
        assertEquals(2, body.getWeekOrders());
        assertEquals(700.0, body.getWeekRevenue());
        // status breakdown includes the cancelled order too
        assertEquals(1L, body.getOrderStatusCounts().get(CafeConstants.ORDER_STATUS_CANCELLED));
        assertEquals(2L, body.getOrderStatusCounts().get(CafeConstants.ORDER_STATUS_DELIVERED));
        // cancelled order's items must not be counted in top products
        assertTrue(body.getTopProducts().stream().noneMatch(p -> p.getName().equals("Farmhouse Pizza")));
        assertEquals(3, body.getTopProducts().stream()
                .filter(p -> p.getName().equals("Margherita Pizza")).findFirst().orElseThrow().getQuantitySold());
    }

    @Test
    void getKitchenQueue_shouldRejectNonAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> dashboardService.getKitchenQueue());
    }

    @Test
    void getKitchenQueue_shouldReturnOnlyInvoiceItems_excludingChargeLines() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Bill placed = bill(300, CafeConstants.ORDER_STATUS_PLACED, Instant.now(),
                invoiceJson("Veg Supreme Pizza", 1, 260.0));
        when(billDao.findByOrderStatusInOrderByCreatedAtAsc(CafeConstants.KITCHEN_QUEUE_STATUSES))
                .thenReturn(List.of(placed));

        ResponseEntity<List<KitchenOrderWrapper>> response = dashboardService.getKitchenQueue();

        assertEquals(1, response.getBody().size());
        KitchenOrderWrapper order = response.getBody().get(0);
        assertEquals(1, order.getItems().size());
        assertEquals("Veg Supreme Pizza", order.getItems().get(0).getName());
    }
}
