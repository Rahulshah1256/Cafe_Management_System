package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Delivery address is required")
    private Integer addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String deliveryInstructions;

    // When true, redeems as many loyalty points as available/needed to discount the order
    // (see CafeConstants.LOYALTY_POINT_VALUE for the conversion rate).
    private Boolean useLoyaltyPoints = Boolean.FALSE;
}
