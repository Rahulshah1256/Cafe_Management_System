package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPartnerWrapper {
    private Integer id;
    private String name;
    private String email;
    private String contactNumber;
    private String vehicleNumber;
    private String availability;
}
