package com.inn.cafe.dao;

import com.inn.cafe.POJO.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartDao extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(Integer userId);
}
