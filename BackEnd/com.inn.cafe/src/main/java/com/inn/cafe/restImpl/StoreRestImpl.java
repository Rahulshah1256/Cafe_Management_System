package com.inn.cafe.restImpl;

import com.inn.cafe.dto.StoreRequest;
import com.inn.cafe.rest.StoreRest;
import com.inn.cafe.service.StoreService;
import com.inn.cafe.wrapper.StoreWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreRestImpl implements StoreRest {

    @Autowired
    StoreService storeService;

    @Override
    public ResponseEntity<List<StoreWrapper>> getActiveStores() {
        return storeService.getActiveStores();
    }

    @Override
    public ResponseEntity<List<StoreWrapper>> getAllStores() {
        return storeService.getAllStores();
    }

    @Override
    public ResponseEntity<StoreWrapper> addStore(StoreRequest request) {
        return storeService.addStore(request);
    }

    @Override
    public ResponseEntity<StoreWrapper> updateStore(Integer id, StoreRequest request) {
        return storeService.updateStore(id, request);
    }

    @Override
    public ResponseEntity<String> deleteStore(Integer id) {
        return storeService.deleteStore(id);
    }
}
