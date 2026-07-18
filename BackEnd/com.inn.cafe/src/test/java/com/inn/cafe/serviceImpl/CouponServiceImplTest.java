package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Coupon;
import com.inn.cafe.dao.CouponDao;
import com.inn.cafe.dto.CouponRequest;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock private CouponDao couponDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponRequest validRequest() {
        CouponRequest request = new CouponRequest();
        request.setCode("WELCOME10");
        request.setDiscountType("PERCENTAGE");
        request.setDiscountValue(10.0);
        return request;
    }

    @Test
    void addCoupon_shouldThrowUnauthorized_whenCallerNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> couponService.addCoupon(validRequest()));
    }

    @Test
    void addCoupon_shouldThrowValidation_whenCodeAlreadyExists() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(couponDao.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(new Coupon()));

        assertThrows(ValidationException.class, () -> couponService.addCoupon(validRequest()));
    }

    @Test
    void addCoupon_shouldThrowValidation_whenDiscountTypeInvalid() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        CouponRequest request = validRequest();
        request.setDiscountType("BOGUS");

        assertThrows(ValidationException.class, () -> couponService.addCoupon(request));
    }

    @Test
    void addCoupon_shouldSave_whenValidAndAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(couponDao.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.empty());
        when(couponDao.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = couponService.addCoupon(validRequest());

        assertEquals("WELCOME10", response.getBody().getCode());
        verify(couponDao).save(any(Coupon.class));
    }
}
