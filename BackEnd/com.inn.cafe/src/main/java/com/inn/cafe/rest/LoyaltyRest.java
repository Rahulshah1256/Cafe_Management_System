package com.inn.cafe.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping(path = "/loyalty")
public interface LoyaltyRest {

    @GetMapping(path = "/balance")
    ResponseEntity<Map<String, Object>> getBalance();
}
