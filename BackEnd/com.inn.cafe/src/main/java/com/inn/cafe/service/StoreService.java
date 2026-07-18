package com.inn.cafe.service;

import com.inn.cafe.dto.StoreRequest;
import com.inn.cafe.wrapper.StoreWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StoreService {
    ResponseEntity<List<StoreWrapper>> getActiveStores();

    ResponseEntity<List<StoreWrapper>> getAllStores();

    ResponseEntity<StoreWrapper> addStore(StoreRequest request);

    ResponseEntity<StoreWrapper> updateStore(Integer id, StoreRequest request);

    ResponseEntity<String> deleteStore(Integer id);
}
