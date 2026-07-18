package com.inn.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryAvailabilityRequest {

    @NotBlank(message = "Status is required")
    private String status;
}
