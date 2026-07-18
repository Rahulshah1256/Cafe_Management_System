package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serializable;

// Store Locator module: physical cafe outlet locations, managed by admins and browsed by
// customers to find the nearest branch / get directions.
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "store")
public class Store implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "pincode", nullable = false)
    private String pincode;

    // Used to build a "Get Directions" Google Maps link on the frontend.
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "phone")
    private String phone;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;
}
