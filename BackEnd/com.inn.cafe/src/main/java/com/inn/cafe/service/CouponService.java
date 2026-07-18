package com.inn.cafe.service;

import com.inn.cafe.dto.CouponRequest;
import com.inn.cafe.wrapper.CouponWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CouponService {
    ResponseEntity<List<CouponWrapper>> getAllCoupons();

    ResponseEntity<CouponWrapper> addCoupon(CouponRequest request);

    ResponseEntity<CouponWrapper> updateCoupon(Integer id, CouponRequest request);

    ResponseEntity<String> deleteCoupon(Integer id);
}
