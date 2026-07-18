package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryAssignRequest {

    @NotBlank(message = "Delivery partner email is required")
    private String partnerEmail;
}
