package com.inn.cafe.service;

import com.inn.cafe.dto.AddToCartRequest;
import com.inn.cafe.dto.ApplyCouponRequest;
import com.inn.cafe.dto.CheckoutRequest;
import com.inn.cafe.dto.UpdateCartItemRequest;
import com.inn.cafe.wrapper.CartWrapper;
import org.springframework.http.ResponseEntity;

public interface CartService {
    ResponseEntity<CartWrapper> getCart();

    ResponseEntity<CartWrapper> addItem(AddToCartRequest request);

    ResponseEntity<CartWrapper> updateItem(Integer itemId, UpdateCartItemRequest request);

    ResponseEntity<CartWrapper> removeItem(Integer itemId);

    ResponseEntity<CartWrapper> clearCart();

    ResponseEntity<CartWrapper> applyCoupon(ApplyCouponRequest request);

    ResponseEntity<CartWrapper> removeCoupon();

    ResponseEntity<String> checkout(CheckoutRequest request);
}
