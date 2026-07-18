package com.inn.cafe.service;

import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.dto.ProductSearchRequest;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface productService {
    ResponseEntity<String> addNewProduct(Map<String, String> requestMap);

    ResponseEntity<List<ProductWrapper>> getAllProduct();

    ResponseEntity<Page<ProductWrapper>> getAllProductPaged(int page, int size, String sortBy, String direction);

    ResponseEntity<Page<ProductWrapper>> searchProducts(ProductSearchRequest request);

    ResponseEntity<String> update(Map<String, String> requestMap);

    ResponseEntity<String> delete(Integer id);

    ResponseEntity<List<ProductWrapper>> getByCategory(Integer id);

    ResponseEntity<ProductWrapper> getProductById(Integer id);
    @Modifying
    @Transactional
    ResponseEntity<String> updateProductStatus(Map<String, String> requestMap);

}
