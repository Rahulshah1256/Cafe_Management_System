package com.inn.cafe.rest;

import com.inn.cafe.dto.AddToCartRequest;
import com.inn.cafe.dto.ApplyCouponRequest;
import com.inn.cafe.dto.CheckoutRequest;
import com.inn.cafe.dto.UpdateCartItemRequest;
import com.inn.cafe.wrapper.CartWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/cart")
public interface CartRest {

    @GetMapping(path = "/get")
    ResponseEntity<CartWrapper> getCart();

    @PostMapping(path = "/add")
    ResponseEntity<CartWrapper> addItem(@Valid @RequestBody AddToCartRequest request);

    @PutMapping(path = "/item/{itemId}")
    ResponseEntity<CartWrapper> updateItem(@PathVariable Integer itemId, @Valid @RequestBody UpdateCartItemRequest request);

    @DeleteMapping(path = "/item/{itemId}")
    ResponseEntity<CartWrapper> removeItem(@PathVariable Integer itemId);

    @DeleteMapping(path = "/clear")
    ResponseEntity<CartWrapper> clearCart();

    @PostMapping(path = "/coupon/apply")
    ResponseEntity<CartWrapper> applyCoupon(@Valid @RequestBody ApplyCouponRequest request);

    @DeleteMapping(path = "/coupon/remove")
    ResponseEntity<CartWrapper> removeCoupon();

    @PostMapping(path = "/checkout")
    ResponseEntity<String> checkout(@Valid @RequestBody CheckoutRequest request);
}
