package com.inn.cafe.dao;

import com.inn.cafe.POJO.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponDao extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
