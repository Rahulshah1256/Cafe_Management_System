package com.inn.cafe.restImpl;

import com.inn.cafe.dto.CouponRequest;
import com.inn.cafe.rest.CouponRest;
import com.inn.cafe.service.CouponService;
import com.inn.cafe.wrapper.CouponWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CouponRestImpl implements CouponRest {

    @Autowired
    CouponService couponService;

    @Override
    public ResponseEntity<List<CouponWrapper>> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @Override
    public ResponseEntity<CouponWrapper> addCoupon(CouponRequest request) {
        return couponService.addCoupon(request);
    }

    @Override
    public ResponseEntity<CouponWrapper> updateCoupon(Integer id, CouponRequest request) {
        return couponService.updateCoupon(id, request);
    }

    @Override
    public ResponseEntity<String> deleteCoupon(Integer id) {
        return couponService.deleteCoupon(id);
    }
}
