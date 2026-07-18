package com.inn.cafe.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductWrapper {
    Integer id;
    String name;
    String description;
    Integer price;
    String status;
    Integer categoryId;
    String categoryName;
    Boolean isVeg;
    String spicyLevel;
    Boolean bestSeller;
    Boolean newArrival;
    Double rating;
    Integer ratingCount;
    String imageUrl;
    Integer prepTimeMinutes;

    public ProductWrapper(Integer id, String name , String description , Integer price , Integer categoryId , String categoryName , String status ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price, Integer categoryId,
                           String categoryName, String status, Boolean isVeg, String spicyLevel, Boolean bestSeller,
                           Boolean newArrival, Double rating, Integer ratingCount, String imageUrl, Integer prepTimeMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
        this.isVeg = isVeg;
        this.spicyLevel = spicyLevel;
        this.bestSeller = bestSeller;
        this.newArrival = newArrival;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.imageUrl = imageUrl;
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
