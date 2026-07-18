package com.inn.cafe.serviceImpl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.service.RecommendationService;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// "AI recommendations": a rule-based (not ML) engine that mines each customer's own order
// history for two signals - items they re-order often ("Order again") and categories they
// favor but haven't fully explored yet ("Discover more") - falling back to a global
// best-seller/top-rated list for guests/new users with no order history yet.
@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 8;
    private static final int MAX_REORDER_ITEMS = 4;
    private static final int TOP_CATEGORY_COUNT = 2;

    @Autowired
    BillDao billDao;

    @Autowired
    productDao productDao;

    @Autowired
    JwtFilter jwtFilter;

    private static final Gson GSON = new Gson();

    @Override
    public ResponseEntity<List<ProductWrapper>> getRecommendations() {
        List<ProductWrapper> activeProducts = productDao.getAllProduct().stream()
                .filter(p -> "true".equalsIgnoreCase(p.getStatus()))
                .toList();

        String username = jwtFilter.getCurrentUsername();
        List<Bill> history = username == null ? List.of() : billDao.getBillByUserName(username);

        List<ProductWrapper> result;
        if (history.isEmpty()) {
            result = new ArrayList<>();
        } else {
            result = personalizedRecommendations(activeProducts, history);
        }

        if (result.size() < MAX_RECOMMENDATIONS) {
            fillWithPopular(result, activeProducts);
        }

        return new ResponseEntity<>(result.stream().limit(MAX_RECOMMENDATIONS).toList(), HttpStatus.OK);
    }

    private List<ProductWrapper> personalizedRecommendations(List<ProductWrapper> activeProducts, List<Bill> history) {
        Map<Integer, ProductWrapper> productsById = new LinkedHashMap<>();
        activeProducts.forEach(p -> productsById.put(p.getId(), p));

        Map<Integer, Integer> productFrequency = new LinkedHashMap<>();
        Map<String, Integer> categoryFrequency = new LinkedHashMap<>();

        for (Bill bill : history) {
            for (Map<String, Object> line : parseInvoiceLines(bill)) {
                if (!isProductLine(line)) {
                    continue;
                }
                Integer productId = parseIntOrNull(line.get("productId"));
                if (productId != null) {
                    productFrequency.merge(productId, 1, Integer::sum);
                }
                Object category = line.get("category");
                if (category != null) {
                    categoryFrequency.merge(String.valueOf(category), 1, Integer::sum);
                }
            }
        }

        List<ProductWrapper> combined = new ArrayList<>();
        Set<Integer> seen = new LinkedHashSet<>();

        // Signal 1: "Order again" - the customer's own most-frequently-ordered active products.
        productFrequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(e -> productsById.get(e.getKey()))
                .filter(java.util.Objects::nonNull)
                .limit(MAX_REORDER_ITEMS)
                .forEach(p -> {
                    if (seen.add(p.getId())) {
                        combined.add(p);
                    }
                });

        // Signal 2: "Discover more" - active, not-yet-tried products from the customer's
        // favorite categories, ranked by best-seller flag then rating.
        List<String> topCategories = categoryFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(TOP_CATEGORY_COUNT)
                .map(Map.Entry::getKey)
                .toList();

        activeProducts.stream()
                .filter(p -> topCategories.contains(p.getCategoryName()))
                .filter(p -> !productFrequency.containsKey(p.getId()))
                .sorted(popularityComparator())
                .forEach(p -> {
                    if (seen.add(p.getId()) && combined.size() < MAX_RECOMMENDATIONS) {
                        combined.add(p);
                    }
                });

        return combined;
    }

    private void fillWithPopular(List<ProductWrapper> result, List<ProductWrapper> activeProducts) {
        Set<Integer> seen = new LinkedHashSet<>();
        result.forEach(p -> seen.add(p.getId()));
        activeProducts.stream()
                .sorted(popularityComparator())
                .forEach(p -> {
                    if (result.size() < MAX_RECOMMENDATIONS && seen.add(p.getId())) {
                        result.add(p);
                    }
                });
    }

    private Comparator<ProductWrapper> popularityComparator() {
        return Comparator.comparing((ProductWrapper p) -> Boolean.TRUE.equals(p.getBestSeller()) ? 0 : 1)
                .thenComparing(p -> -1 * (p.getRating() == null ? 0.0 : p.getRating()));
    }

    private boolean isProductLine(Map<String, Object> line) {
        Object category = line.get("category");
        return category == null || !CafeConstants.INVOICE_LINE_CHARGES_CATEGORY.equals(category);
    }

    private Integer parseIntOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
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
}
