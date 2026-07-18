package com.inn.cafe.rest;

import com.inn.cafe.dto.ProductSearchRequest;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/product")
public interface productRest {
    @PostMapping(path = "/add")
    public ResponseEntity<String> addNewProduct(@RequestBody Map<String, String> requestMap);

    @GetMapping(path = "/get")
    public ResponseEntity<List<ProductWrapper>> getAllProduct();

    // AI recommendations: personalized "Order again" + "Discover more" list built from the
    // caller's own order history (falls back to global best-sellers for new users/guests).
    @GetMapping(path = "/recommendations")
    public ResponseEntity<List<ProductWrapper>> getRecommendations();

    @GetMapping(path = "/get/paged")
    public ResponseEntity<Page<ProductWrapper>> getAllProductPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction);

    @GetMapping(path = "/search")
    public ResponseEntity<Page<ProductWrapper>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isVeg,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String spicyLevel,
            @RequestParam(required = false) Boolean bestSeller,
            @RequestParam(required = false) Boolean newArrival,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction);

    @PostMapping(path = "/update")
    public ResponseEntity<String> update(@RequestBody(required = true) Map<String, String> requestMap);

    @PostMapping(path = "/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id);

    @GetMapping(path = "/getByCategory/{id}")
    public ResponseEntity<List<ProductWrapper>> getByCategory(@PathVariable Integer id);

    @GetMapping(path = "/getProductById/{id}")
    public ResponseEntity<ProductWrapper> getProductById(@PathVariable Integer id);

    @PostMapping(path = "/updateProductStatus")
    public ResponseEntity<String> updateProductStatus(@RequestBody(required = true) Map<String, String> requestMap);

    /*
    @PostMapping(path = "/updateProductStatus")
    public ResponseEntity<String> updateProductStatus(@RequestBody Map<String, String> requestMap);
    */
}
