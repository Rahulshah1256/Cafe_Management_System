package com.inn.cafe.rest;

import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.dto.VerifySignupOtpRequest;
import com.inn.cafe.wrapper.UserWrapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/user")
public interface UserRest {
    @PostMapping(path = "/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest request);

    @PostMapping(path = "/verifySignupOtp")
    public ResponseEntity<String> verifySignupOtp(@Valid @RequestBody VerifySignupOtpRequest request);

    @PostMapping(path = "/resendSignupOtp")
    public ResponseEntity<String> resendSignupOtp(@RequestBody Map<String, String> requestMap);

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request);

    @GetMapping(path = "/get")
    public ResponseEntity<List<UserWrapper>> getAllUser();

    @GetMapping(path = "/get/paged")
    public ResponseEntity<Page<UserWrapper>> getAllUserPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction);

    @PostMapping(path = "/update")
    public ResponseEntity<String> update(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/checkToken")
    public ResponseEntity<String> checkToken();

    @PostMapping(path = "/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path = "/forgotPassword")
    public ResponseEntity<String> forgetPassword(@RequestBody Map<String, String> requestMap);
}

