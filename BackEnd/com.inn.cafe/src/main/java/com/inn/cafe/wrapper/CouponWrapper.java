package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponWrapper {
    private Integer id;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderAmount;
    private LocalDate expiryDate;
    private Boolean active;
    private Integer usageLimit;
    private Integer usedCount;
}
