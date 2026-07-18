package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Store;
import com.inn.cafe.dao.StoreDao;
import com.inn.cafe.dto.StoreRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock private StoreDao storeDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private StoreServiceImpl storeService;

    private StoreRequest validRequest() {
        StoreRequest request = new StoreRequest();
        request.setName("Cafe Central");
        request.setAddressLine1("MG Road");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPincode("560001");
        request.setLatitude(12.9716);
        request.setLongitude(77.5946);
        return request;
    }

    private Store store() {
        Store s = new Store();
        s.setId(1);
        s.setName("Cafe Central");
        s.setActive(true);
        return s;
    }

    @Test
    void getActiveStores_shouldNotRequireAdmin() {
        when(storeDao.findByActiveTrue()).thenReturn(List.of(store()));

        ResponseEntity<List<com.inn.cafe.wrapper.StoreWrapper>> response = storeService.getActiveStores();

        assertEquals(1, response.getBody().size());
        verifyNoInteractions(jwtFilter);
    }

    @Test
    void getAllStores_shouldRejectNonAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> storeService.getAllStores());
    }

    @Test
    void addStore_shouldRejectNonAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> storeService.addStore(validRequest()));
    }

    @Test
    void addStore_shouldSucceed() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(storeDao.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<com.inn.cafe.wrapper.StoreWrapper> response = storeService.addStore(validRequest());

        assertEquals("Cafe Central", response.getBody().getName());
        assertTrue(response.getBody().getActive());
    }

    @Test
    void updateStore_shouldThrowNotFound_whenMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(storeDao.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storeService.updateStore(99, validRequest()));
    }

    @Test
    void updateStore_shouldSucceed() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(storeDao.findById(1)).thenReturn(Optional.of(store()));
        when(storeDao.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreRequest request = validRequest();
        request.setName("Cafe Central Updated");
        ResponseEntity<com.inn.cafe.wrapper.StoreWrapper> response = storeService.updateStore(1, request);

        assertEquals("Cafe Central Updated", response.getBody().getName());
    }

    @Test
    void deleteStore_shouldThrowNotFound_whenMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(storeDao.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storeService.deleteStore(99));
    }

    @Test
    void deleteStore_shouldSucceed() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Store s = store();
        when(storeDao.findById(1)).thenReturn(Optional.of(s));

        ResponseEntity<String> response = storeService.deleteStore(1);

        verify(storeDao).delete(s);
        assertTrue(response.getBody().contains("deleted"));
    }
}
