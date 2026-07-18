package com.inn.cafe.service;

import com.inn.cafe.dto.AddressRequest;
import com.inn.cafe.wrapper.AddressWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService {
    ResponseEntity<List<AddressWrapper>> getMyAddresses();

    ResponseEntity<AddressWrapper> addAddress(AddressRequest request);

    ResponseEntity<AddressWrapper> updateAddress(Integer id, AddressRequest request);

    ResponseEntity<String> deleteAddress(Integer id);

    ResponseEntity<AddressWrapper> setDefault(Integer id);
}
