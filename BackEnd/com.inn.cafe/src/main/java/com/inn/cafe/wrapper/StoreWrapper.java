package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreWrapper {
    Integer id;
    String name;
    String addressLine1;
    String addressLine2;
    String city;
    String state;
    String pincode;
    Double latitude;
    Double longitude;
    String phone;
    String openingHours;
    Boolean active;
}
