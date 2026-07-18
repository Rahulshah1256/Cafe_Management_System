package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Address;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.AddressDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.AddressRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.service.AddressService;
import com.inn.cafe.wrapper.AddressWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressDao addressDao;

    @Autowired
    UserDao userDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<List<AddressWrapper>> getMyAddresses() {
        User user = currentUser();
        List<AddressWrapper> wrappers = addressDao.findByUserIdOrderByIsDefaultDescIdDesc(user.getId())
                .stream().map(this::toWrapper).toList();
        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddressWrapper> addAddress(AddressRequest request) {
        User user = currentUser();
        Address address = new Address();
        address.setUser(user);
        applyRequest(address, request);

        boolean isFirstAddress = addressDao.findByUserIdOrderByIsDefaultDescIdDesc(user.getId()).isEmpty();
        if (Boolean.TRUE.equals(request.getIsDefault()) || isFirstAddress) {
            clearExistingDefault(user.getId());
            address.setIsDefault(true);
        }
        addressDao.save(address);
        log.info("Address added for user {}", user.getEmail());
        return new ResponseEntity<>(toWrapper(address), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddressWrapper> updateAddress(Integer id, AddressRequest request) {
        Address address = getOwnedAddress(id);
        applyRequest(address, request);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearExistingDefault(address.getUser().getId());
            address.setIsDefault(true);
        }
        addressDao.save(address);
        return new ResponseEntity<>(toWrapper(address), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteAddress(Integer id) {
        Address address = getOwnedAddress(id);
        addressDao.delete(address);
        return new ResponseEntity<>("{\"message\":\"Address deleted successfully\"}", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddressWrapper> setDefault(Integer id) {
        Address address = getOwnedAddress(id);
        clearExistingDefault(address.getUser().getId());
        address.setIsDefault(true);
        addressDao.save(address);
        return new ResponseEntity<>(toWrapper(address), HttpStatus.OK);
    }

    private void applyRequest(Address address, AddressRequest request) {
        address.setLabel(request.getLabel());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());
        address.setContactNumber(request.getContactNumber());
        if (request.getAddressType() != null) {
            address.setAddressType(request.getAddressType());
        }
    }

    private void clearExistingDefault(Integer userId) {
        List<Address> addresses = addressDao.findByUserIdOrderByIsDefaultDescIdDesc(userId);
        addresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .forEach(a -> {
                    a.setIsDefault(false);
                    addressDao.save(a);
                });
    }

    private Address getOwnedAddress(Integer id) {
        User user = currentUser();
        Address address = addressDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This address does not belong to you");
        }
        return address;
    }

    private User currentUser() {
        User user = userDao.findByEmail(jwtFilter.getCurrentUsername());
        if (user == null) {
            throw new ResourceNotFoundException("Current user not found");
        }
        return user;
    }

    private AddressWrapper toWrapper(Address a) {
        return new AddressWrapper(a.getId(), a.getLabel(), a.getAddressLine1(), a.getAddressLine2(),
                a.getCity(), a.getState(), a.getPincode(), a.getLandmark(), a.getContactNumber(),
                a.getAddressType(), a.getIsDefault());
    }
}
