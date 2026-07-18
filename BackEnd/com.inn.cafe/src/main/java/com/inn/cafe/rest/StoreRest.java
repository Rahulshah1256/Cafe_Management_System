package com.inn.cafe.rest;

import com.inn.cafe.dto.StoreRequest;
import com.inn.cafe.wrapper.StoreWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/store")
public interface StoreRest {

    // All authenticated users (any role) may browse active store locations.
    @GetMapping(path = "/list")
    ResponseEntity<List<StoreWrapper>> getActiveStores();

    @GetMapping(path = "/all")
    ResponseEntity<List<StoreWrapper>> getAllStores();

    @PostMapping(path = "/add")
    ResponseEntity<StoreWrapper> addStore(@Valid @RequestBody StoreRequest request);

    @PutMapping(path = "/update/{id}")
    ResponseEntity<StoreWrapper> updateStore(@PathVariable Integer id, @Valid @RequestBody StoreRequest request);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteStore(@PathVariable Integer id);
}
