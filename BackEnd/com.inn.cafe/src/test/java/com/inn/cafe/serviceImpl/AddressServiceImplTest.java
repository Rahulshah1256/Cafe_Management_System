package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Address;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.AddressDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.AddressRequest;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.wrapper.AddressWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock private AddressDao addressDao;
    @Mock private UserDao userDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("user@cafe.com");
        lenient().when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        lenient().when(userDao.findByEmail("user@cafe.com")).thenReturn(user);
        lenient().when(addressDao.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(addressDao.findByUserIdOrderByIsDefaultDescIdDesc(1)).thenReturn(Collections.emptyList());
    }

    private AddressRequest validRequest() {
        AddressRequest request = new AddressRequest();
        request.setAddressLine1("221B Baker Street");
        request.setCity("London");
        request.setState("LDN");
        request.setPincode("560001");
        request.setContactNumber("9000000000");
        return request;
    }

    @Test
    void addAddress_shouldSaveAddress_ownedByCurrentUser() {
        ResponseEntity<AddressWrapper> response = addressService.addAddress(validRequest());

        assertEquals(200, response.getStatusCode().value());
        verify(addressDao).save(any(Address.class));
    }

    @Test
    void updateAddress_shouldThrowUnauthorized_whenAddressBelongsToAnotherUser() {
        User otherUser = new User();
        otherUser.setId(2);
        Address address = new Address();
        address.setId(10);
        address.setUser(otherUser);
        when(addressDao.findById(10)).thenReturn(Optional.of(address));

        assertThrows(UnauthorizedException.class, () -> addressService.updateAddress(10, validRequest()));
    }

    @Test
    void addAddress_shouldClearPreviousDefault_whenNewAddressMarkedDefault() {
        Address existingDefault = new Address();
        existingDefault.setId(3);
        existingDefault.setUser(user);
        existingDefault.setIsDefault(true);
        when(addressDao.findByUserIdOrderByIsDefaultDescIdDesc(1)).thenReturn(Collections.singletonList(existingDefault));

        AddressRequest request = validRequest();
        request.setIsDefault(true);
        addressService.addAddress(request);

        assertFalse(existingDefault.getIsDefault());
        verify(addressDao, atLeastOnce()).save(existingDefault);
    }

    @Test
    void deleteAddress_shouldThrowUnauthorized_whenNotOwner() {
        User otherUser = new User();
        otherUser.setId(2);
        Address address = new Address();
        address.setId(10);
        address.setUser(otherUser);
        when(addressDao.findById(10)).thenReturn(Optional.of(address));

        assertThrows(UnauthorizedException.class, () -> addressService.deleteAddress(10));
    }
}
