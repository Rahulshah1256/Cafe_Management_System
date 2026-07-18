package com.inn.cafe.dao;

import com.inn.cafe.POJO.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemDao extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);
    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);
    void deleteByCartId(Integer cartId);
}
