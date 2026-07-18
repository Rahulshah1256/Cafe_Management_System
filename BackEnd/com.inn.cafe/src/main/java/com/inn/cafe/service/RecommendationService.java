package com.inn.cafe.service;

import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RecommendationService {
    ResponseEntity<List<ProductWrapper>> getRecommendations();
}
