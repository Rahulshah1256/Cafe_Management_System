package com.inn.cafe.service;

import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(SignUpRequest request);

    ResponseEntity<String> login(LoginRequest request);

    ResponseEntity<List<UserWrapper>> getAllUser();

    ResponseEntity<Page<UserWrapper>> getAllUserPaged(int page, int size, String sortBy, String direction);

    ResponseEntity<String> update(Map<String, String> requestMap);

    ResponseEntity<String> checkToken();

    ResponseEntity<String> changePassword(Map<String, String> requestMap);

    ResponseEntity<String> forgetPassword(Map<String, String> requestMap);
}

