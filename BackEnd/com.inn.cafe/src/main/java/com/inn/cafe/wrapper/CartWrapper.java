package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartWrapper {
    private List<CartItemWrapper> items;
    private Integer itemCount;
    private Double subtotal;
    private String appliedCouponCode;
    private Double discount;
    private Double deliveryCharge;
    private Double packingCharge;
    private Double platformFee;
    private Double tax;
    private Double total;
}
