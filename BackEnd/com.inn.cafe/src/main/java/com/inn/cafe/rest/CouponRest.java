package com.inn.cafe.rest;

import com.inn.cafe.dto.CouponRequest;
import com.inn.cafe.wrapper.CouponWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/coupon")
public interface CouponRest {

    @GetMapping(path = "/get")
    ResponseEntity<List<CouponWrapper>> getAllCoupons();

    @PostMapping(path = "/add")
    ResponseEntity<CouponWrapper> addCoupon(@Valid @RequestBody CouponRequest request);

    @PutMapping(path = "/update/{id}")
    ResponseEntity<CouponWrapper> updateCoupon(@PathVariable Integer id, @Valid @RequestBody CouponRequest request);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCoupon(@PathVariable Integer id);
}
