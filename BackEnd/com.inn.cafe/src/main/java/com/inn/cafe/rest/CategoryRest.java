package com.inn.cafe.rest;


import com.inn.cafe.POJO.Category;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/category")
public interface CategoryRest {
    @PostMapping(path = "/add")
    public ResponseEntity<String> addNewCategory(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    public ResponseEntity<List<Category>> getAllCategory(@RequestParam(required = false) String Value);

    @GetMapping(path = "/get/paged")
    public ResponseEntity<Page<Category>> getAllCategoryPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction);

    @PostMapping(path = "/update")
    public ResponseEntity<String> update(@RequestBody(required = true) Map<String, String> requestMap);

}


