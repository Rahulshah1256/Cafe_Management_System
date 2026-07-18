package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemWrapper {
    private Integer id;
    private Integer productId;
    private String productName;
    private String imageUrl;
    private Boolean isVeg;
    private Integer unitPrice;
    private Integer quantity;
    private Integer subtotal;
    private Boolean available;
}
