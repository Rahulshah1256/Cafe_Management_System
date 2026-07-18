package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Coupon;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.CouponDao;
import com.inn.cafe.dto.CouponRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.CouponService;
import com.inn.cafe.wrapper.CouponWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    CouponDao couponDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<List<CouponWrapper>> getAllCoupons() {
        requireAdmin();
        List<CouponWrapper> wrappers = couponDao.findAll().stream().map(this::toWrapper).toList();
        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CouponWrapper> addCoupon(CouponRequest request) {
        requireAdmin();
        validateDiscountType(request.getDiscountType());
        couponDao.findByCodeIgnoreCase(request.getCode()).ifPresent(c -> {
            throw new ValidationException("Coupon code already exists: " + request.getCode());
        });

        Coupon coupon = new Coupon();
        applyRequest(coupon, request);
        couponDao.save(coupon);
        log.info("Coupon created: {}", coupon.getCode());
        return new ResponseEntity<>(toWrapper(coupon), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CouponWrapper> updateCoupon(Integer id, CouponRequest request) {
        requireAdmin();
        validateDiscountType(request.getDiscountType());
        Coupon coupon = couponDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        applyRequest(coupon, request);
        couponDao.save(coupon);
        return new ResponseEntity<>(toWrapper(coupon), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteCoupon(Integer id) {
        requireAdmin();
        Coupon coupon = couponDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        couponDao.delete(coupon);
        return new ResponseEntity<>("{\"message\":\"Coupon deleted successfully\"}", HttpStatus.OK);
    }

    private void validateDiscountType(String type) {
        if (!"PERCENTAGE".equalsIgnoreCase(type) && !"FLAT".equalsIgnoreCase(type)) {
            throw new ValidationException("Discount type must be PERCENTAGE or FLAT");
        }
    }

    private void applyRequest(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType().toUpperCase());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderAmount(request.getMinOrderAmount() == null ? 0.0 : request.getMinOrderAmount());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        coupon.setUsageLimit(request.getUsageLimit());
    }

    private void requireAdmin() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
    }

    private CouponWrapper toWrapper(Coupon c) {
        return new CouponWrapper(c.getId(), c.getCode(), c.getDescription(), c.getDiscountType(),
                c.getDiscountValue(), c.getMaxDiscountAmount(), c.getMinOrderAmount(), c.getExpiryDate(),
                c.getActive(), c.getUsageLimit(), c.getUsedCount());
    }
}
