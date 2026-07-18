package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;

    // PERCENTAGE or FLAT
    @NotBlank(message = "Discount type is required")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double discountValue;

    private Double maxDiscountAmount;

    private Double minOrderAmount = 0.0;

    private LocalDate expiryDate;

    private Boolean active = Boolean.TRUE;

    private Integer usageLimit;
}
