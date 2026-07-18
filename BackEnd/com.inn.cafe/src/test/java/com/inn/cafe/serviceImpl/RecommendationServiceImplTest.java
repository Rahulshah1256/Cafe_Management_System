package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.wrapper.ProductWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock private BillDao billDao;
    @Mock private productDao productDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private ProductWrapper product(int id, String name, String category, boolean bestSeller, double rating) {
        return new ProductWrapper(id, name, "desc", 100, id, category, "true", true, "NONE",
                bestSeller, false, rating, 10, null, 10);
    }

    private Bill billWithLines(String username, String... productIdAndCategoryPairs) {
        // productIdAndCategoryPairs: alternating productId, category e.g. "1", "Pizza"
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productIdAndCategoryPairs.length; i += 2) {
            if (i > 0) json.append(",");
            json.append("{\"name\":\"Item").append(productIdAndCategoryPairs[i]).append("\",")
                    .append("\"category\":\"").append(productIdAndCategoryPairs[i + 1]).append("\",")
                    .append("\"quantity\":\"1\",\"price\":100.0,\"total\":100.0,")
                    .append("\"productId\":").append(productIdAndCategoryPairs[i]).append("}");
        }
        json.append("]");
        Bill bill = new Bill();
        bill.setId(1);
        bill.setCreatedBy(username);
        bill.setProductDetails(json.toString());
        return bill;
    }

    @Test
    void getRecommendations_shouldFallBackToPopular_whenNoOrderHistory() {
        when(jwtFilter.getCurrentUsername()).thenReturn("newuser@cafe.com");
        when(billDao.getBillByUserName("newuser@cafe.com")).thenReturn(List.of());
        when(productDao.getAllProduct()).thenReturn(List.of(
                product(1, "Margherita", "Pizza", false, 3.5),
                product(2, "Farmhouse", "Pizza", true, 4.5),
                product(3, "Coke", "Beverages", false, 4.0)
        ));

        ResponseEntity<List<ProductWrapper>> response = recommendationService.getRecommendations();

        List<ProductWrapper> body = response.getBody();
        assertEquals(3, body.size());
        // Best-seller should rank first in the popular fallback.
        assertEquals(2, body.get(0).getId());
    }

    @Test
    void getRecommendations_shouldReorderFrequentlyOrderedItems_first() {
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        Bill bill1 = billWithLines("user@cafe.com", "1", "Pizza");
        Bill bill2 = billWithLines("user@cafe.com", "1", "Pizza");
        when(billDao.getBillByUserName("user@cafe.com")).thenReturn(List.of(bill1, bill2));
        when(productDao.getAllProduct()).thenReturn(List.of(
                product(1, "Margherita", "Pizza", false, 3.5),
                product(2, "Farmhouse", "Pizza", true, 4.5),
                product(3, "Coke", "Beverages", false, 4.0)
        ));

        ResponseEntity<List<ProductWrapper>> response = recommendationService.getRecommendations();

        List<ProductWrapper> body = response.getBody();
        // Product 1 was ordered twice -> should be the top "order again" recommendation.
        assertEquals(1, body.get(0).getId());
        // Product 2 shares the "Pizza" category and hasn't been tried -> discovery pick.
        assertTrue(body.stream().anyMatch(p -> p.getId() == 2));
    }

    @Test
    void getRecommendations_shouldExcludeInactiveProducts() {
        when(jwtFilter.getCurrentUsername()).thenReturn("newuser@cafe.com");
        when(billDao.getBillByUserName("newuser@cafe.com")).thenReturn(List.of());
        when(productDao.getAllProduct()).thenReturn(List.of(
                product(1, "Margherita", "Pizza", true, 4.5),
                new ProductWrapper(2, "Discontinued", "desc", 100, 2, "Pizza", "false", true, "NONE",
                        true, false, 5.0, 10, null, 10)
        ));

        ResponseEntity<List<ProductWrapper>> response = recommendationService.getRecommendations();

        List<ProductWrapper> body = response.getBody();
        assertEquals(1, body.size());
        assertEquals(1, body.get(0).getId());
    }
}
