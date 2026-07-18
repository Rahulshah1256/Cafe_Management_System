package com.inn.cafe.restImpl;

import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.dto.VerifySignupOtpRequest;
import com.inn.cafe.rest.UserRest;
import com.inn.cafe.service.UserService;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserRestImpl implements UserRest {

    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<String> signUp(SignUpRequest request) {
        return userService.signUp(request);
    }

    @Override
    public ResponseEntity<String> verifySignupOtp(VerifySignupOtpRequest request) {
        return userService.verifySignupOtp(request);
    }

    @Override
    public ResponseEntity<String> resendSignupOtp(Map<String, String> requestMap) {
        return userService.resendSignupOtp(requestMap);
    }

    @Override
    public ResponseEntity<String> login(LoginRequest request) {
        return userService.login(request);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        return userService.getAllUser();
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<UserWrapper>> getAllUserPaged(int page, int size, String sortBy, String direction) {
        return userService.getAllUserPaged(page, size, sortBy, direction);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        return userService.update(requestMap);
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return userService.checkToken();
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        return userService.changePassword(requestMap);
    }

    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        return userService.forgetPassword(requestMap);
    }
}

