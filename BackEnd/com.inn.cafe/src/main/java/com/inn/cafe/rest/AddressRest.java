package com.inn.cafe.rest;

import com.inn.cafe.dto.AddressRequest;
import com.inn.cafe.wrapper.AddressWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/address")
public interface AddressRest {

    @GetMapping(path = "/get")
    ResponseEntity<List<AddressWrapper>> getMyAddresses();

    @PostMapping(path = "/add")
    ResponseEntity<AddressWrapper> addAddress(@Valid @RequestBody AddressRequest request);

    @PutMapping(path = "/update/{id}")
    ResponseEntity<AddressWrapper> updateAddress(@PathVariable Integer id, @Valid @RequestBody AddressRequest request);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteAddress(@PathVariable Integer id);

    @PutMapping(path = "/default/{id}")
    ResponseEntity<AddressWrapper> setDefault(@PathVariable Integer id);
}
