package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "coupon")
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "description")
    private String description;

    // PERCENTAGE or FLAT
    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    // Only applicable when discountType = PERCENTAGE; caps the discount amount. Null = no cap.
    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "min_order_amount")
    private Double minOrderAmount = 0.0;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    // Null = unlimited usage
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;
}
