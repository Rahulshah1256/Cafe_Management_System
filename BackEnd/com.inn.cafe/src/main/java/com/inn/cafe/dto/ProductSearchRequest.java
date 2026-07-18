package com.inn.cafe.dto;

import lombok.Data;

/**
 * Filter/search criteria for GET /product/search. All fields are optional; only supplied
 * (non-null) filters are applied. Backed by {@link com.inn.cafe.specification.ProductSpecification}.
 */
@Data
public class ProductSearchRequest {

    private String keyword;
    private Integer categoryId;
    private Boolean isVeg;
    private Integer minPrice;
    private Integer maxPrice;
    private Double minRating;
    private String spicyLevel;
    private Boolean bestSeller;
    private Boolean newArrival;
    private String status;

    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String direction = "asc";
}
