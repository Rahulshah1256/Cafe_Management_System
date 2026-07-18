package com.inn.cafe.restImpl;

import com.inn.cafe.dto.AddressRequest;
import com.inn.cafe.rest.AddressRest;
import com.inn.cafe.service.AddressService;
import com.inn.cafe.wrapper.AddressWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AddressRestImpl implements AddressRest {

    @Autowired
    AddressService addressService;

    @Override
    public ResponseEntity<List<AddressWrapper>> getMyAddresses() {
        return addressService.getMyAddresses();
    }

    @Override
    public ResponseEntity<AddressWrapper> addAddress(AddressRequest request) {
        return addressService.addAddress(request);
    }

    @Override
    public ResponseEntity<AddressWrapper> updateAddress(Integer id, AddressRequest request) {
        return addressService.updateAddress(id, request);
    }

    @Override
    public ResponseEntity<String> deleteAddress(Integer id) {
        return addressService.deleteAddress(id);
    }

    @Override
    public ResponseEntity<AddressWrapper> setDefault(Integer id) {
        return addressService.setDefault(id);
    }
}
