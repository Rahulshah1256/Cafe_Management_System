package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Store;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.StoreDao;
import com.inn.cafe.dto.StoreRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.service.StoreService;
import com.inn.cafe.wrapper.StoreWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    StoreDao storeDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<List<StoreWrapper>> getActiveStores() {
        List<StoreWrapper> stores = storeDao.findByActiveTrue().stream().map(this::toWrapper).toList();
        return new ResponseEntity<>(stores, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StoreWrapper>> getAllStores() {
        requireAdmin();
        List<StoreWrapper> stores = storeDao.findAll().stream().map(this::toWrapper).toList();
        return new ResponseEntity<>(stores, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StoreWrapper> addStore(StoreRequest request) {
        requireAdmin();
        Store store = new Store();
        applyRequest(store, request);
        storeDao.save(store);
        log.info("Store created: {}", store.getName());
        return new ResponseEntity<>(toWrapper(store), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StoreWrapper> updateStore(Integer id, StoreRequest request) {
        requireAdmin();
        Store store = storeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        applyRequest(store, request);
        storeDao.save(store);
        return new ResponseEntity<>(toWrapper(store), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteStore(Integer id) {
        requireAdmin();
        Store store = storeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        storeDao.delete(store);
        return new ResponseEntity<>("{\"message\":\"Store deleted successfully\"}", HttpStatus.OK);
    }

    private void applyRequest(Store store, StoreRequest request) {
        store.setName(request.getName());
        store.setAddressLine1(request.getAddressLine1());
        store.setAddressLine2(request.getAddressLine2());
        store.setCity(request.getCity());
        store.setState(request.getState());
        store.setPincode(request.getPincode());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setPhone(request.getPhone());
        store.setOpeningHours(request.getOpeningHours());
        store.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
    }

    private void requireAdmin() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
    }

    private StoreWrapper toWrapper(Store s) {
        return new StoreWrapper(s.getId(), s.getName(), s.getAddressLine1(), s.getAddressLine2(), s.getCity(),
                s.getState(), s.getPincode(), s.getLatitude(), s.getLongitude(), s.getPhone(),
                s.getOpeningHours(), s.getActive());
    }
}
