package com.inn.cafe.dao;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface productDao extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<ProductWrapper> getAllProduct();

    List<ProductWrapper> getByCategory(@Param("id") Integer id);

    ProductWrapper getProductById(@Param("id") Integer id);

    @Modifying
    @Transactional
    void updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    @Query("select new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.category.id, u.category.name, u.status, u.isVeg, u.spicyLevel, u.bestSeller, u.newArrival, u.rating, u.ratingCount, u.imageUrl, u.prepTimeMinutes) from Product u")
    Page<ProductWrapper> getAllProductPaged(Pageable pageable);

}

