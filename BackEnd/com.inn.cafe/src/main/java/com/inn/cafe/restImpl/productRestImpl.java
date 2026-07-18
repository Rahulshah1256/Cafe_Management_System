package com.inn.cafe.restImpl;

import com.inn.cafe.dao.productDao;
import com.inn.cafe.dto.ProductSearchRequest;
import com.inn.cafe.rest.productRest;
import com.inn.cafe.service.RecommendationService;
import com.inn.cafe.service.productService;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class productRestImpl implements productRest {
    @Autowired
    productService productService;

    @Autowired
    productDao productDao;

    @Autowired
    RecommendationService recommendationService;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        return productService.addNewProduct(requestMap);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        return productService.getAllProduct();
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getRecommendations() {
        return recommendationService.getRecommendations();
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<ProductWrapper>> getAllProductPaged(int page, int size, String sortBy, String direction) {
        return productService.getAllProductPaged(page, size, sortBy, direction);
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<ProductWrapper>> searchProducts(String keyword, Integer categoryId, Boolean isVeg, Integer minPrice, Integer maxPrice, Double minRating, String spicyLevel, Boolean bestSeller, Boolean newArrival, String status, int page, int size, String sortBy, String direction) {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setIsVeg(isVeg);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(minRating);
        request.setSpicyLevel(spicyLevel);
        request.setBestSeller(bestSeller);
        request.setNewArrival(newArrival);
        request.setStatus(status);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setDirection(direction);
        return productService.searchProducts(request);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        return productService.update(requestMap);
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        return productService.delete(id);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        return productService.getByCategory(id);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        return productService.getProductById(id);
    }

    @Override
    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
        return productService.updateProductStatus(requestMap);
    }


}

