package com.inn.cafe.restImpl;

import com.inn.cafe.dto.AddToCartRequest;
import com.inn.cafe.dto.ApplyCouponRequest;
import com.inn.cafe.dto.CheckoutRequest;
import com.inn.cafe.dto.UpdateCartItemRequest;
import com.inn.cafe.rest.CartRest;
import com.inn.cafe.service.CartService;
import com.inn.cafe.wrapper.CartWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartRestImpl implements CartRest {

    @Autowired
    CartService cartService;

    @Override
    public ResponseEntity<CartWrapper> getCart() {
        return cartService.getCart();
    }

    @Override
    public ResponseEntity<CartWrapper> addItem(AddToCartRequest request) {
        return cartService.addItem(request);
    }

    @Override
    public ResponseEntity<CartWrapper> updateItem(Integer itemId, UpdateCartItemRequest request) {
        return cartService.updateItem(itemId, request);
    }

    @Override
    public ResponseEntity<CartWrapper> removeItem(Integer itemId) {
        return cartService.removeItem(itemId);
    }

    @Override
    public ResponseEntity<CartWrapper> clearCart() {
        return cartService.clearCart();
    }

    @Override
    public ResponseEntity<CartWrapper> applyCoupon(ApplyCouponRequest request) {
        return cartService.applyCoupon(request);
    }

    @Override
    public ResponseEntity<CartWrapper> removeCoupon() {
        return cartService.removeCoupon();
    }

    @Override
    public ResponseEntity<String> checkout(CheckoutRequest request) {
        return cartService.checkout(request);
    }
}
